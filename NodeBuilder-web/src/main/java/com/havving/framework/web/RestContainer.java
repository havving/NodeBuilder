package com.havving.framework.web;

import com.google.gson.*;
import com.havving.framework.NodeBuilder;
import com.havving.framework.NodeContext;
import com.havving.framework.annotation.RestBinder;
import com.havving.framework.cluster.ClusterContainer;
import com.havving.framework.cluster.SharedMap;
import com.havving.framework.components.ComponentPolicyFactory;
import com.havving.framework.components.Container;
import com.havving.framework.components.SingletonProxyFactory;
import com.havving.framework.config.extensions.WebApp;
import com.havving.framework.domain.Configuration;
import com.havving.framework.exception.ContainerInitializeException;
import lombok.extern.slf4j.Slf4j;
import spark.*;
import spark.route.RouteOverview;
import spark.utils.IOUtils;

import java.lang.reflect.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author HAVVING
 * @since 2021-04-28
 */
@Slf4j
public class RestContainer implements Container<RestDefineFactory> {
    private Map<String, Method> methodMap;
    private RestDefineFactory restDefineFactory;
    private boolean valid = false;

    public RestContainer() {
        methodMap = new HashMap<>();
    }

    @Override
    public CoreExtensions getExtensionType() {
        return CoreExtensions.WEB;
    }

    @Override
    public RestDefineFactory getFactory() {
        return restDefineFactory;
    }

    @Override
    public Container initializeToScan(String scanPackage) throws ContainerInitializeException {
        restDefineFactory = new RestDefineFactory();
        NodeContext context = NodeBuilder.getContext();
        Container clusterContainer = context.getContainer(CoreExtensions.CLUSTER);

        Configuration globalConf = context.getConfig();
        WebApp webAppConf = globalConf.getNodeConfig().getWebApp();
        log.info("WebApp configuration : {}", webAppConf);

        // RestBinder 스캔
        SingletonProxyFactory singletonProxyFactory = context.getObjectFactory();
        // 정보 획득
        singletonProxyFactory.forEach((key, value) ->
                Stream.of(value.getClass().getSuperclass().getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(RestBinder.class))
                        .forEach(m -> {
                            RestBinder anno = m.getDeclaredAnnotation(RestBinder.class);

                            m.setAccessible(true);
                            // RestDefine 생성
                            RestDefine define = new RestDefine();
                            define.setComponentId(key);
                            define.setMethodName(m.getName());
                            define.setUrl(anno.url());
                            define.setAccept(anno.method());

                            Parameter[] methodParameters = m.getParameters();
                            if (methodParameters != null && methodParameters.length > 0) {
                                RestDefine.MethodArgs[] methodArgs = new RestDefine.MethodArgs[methodParameters.length];

                                for (int i = 0; i < methodArgs.length; i++) {
                                    Parameter p = methodParameters[i];
                                    Type type = p.getParameterizedType();
                                    if (type instanceof Class) {
                                        methodArgs[i] = new RestDefine.MethodArgs(p.getName(), p.getType());
                                    } else if (type instanceof ParameterizedType) {
                                        Type[] actualGenericTypes = ((ParameterizedType) p.getParameterizedType()).getActualTypeArguments();
                                        methodArgs[i] = new RestDefine.MethodArgs(p.getName(), p.getType(), actualGenericTypes);
                                    }
                                }
                                define.setMethodArgs(methodArgs);
                            }
                            define.setContentType(anno.contentType().toString());
                            define.setReturnType(m.getReturnType().getName());
                            m.setAccessible(false);
                            restDefineFactory.put(anno.url(), define);
                            methodMap.put(anno.url(), m);
                        })
        );
        _initServer(webAppConf, singletonProxyFactory);

        if (clusterContainer != null) {
            _initCluster((ClusterContainer) clusterContainer);
        } else {
            _initLocalConf(globalConf, context.getPolicyFactory());
        }

        RouteOverview.enableRouteOverview(webAppConf.getContext() + "/api");
        Spark.awaitInitialization();
        valid = true;

        return this;
    }


    private void _initServer(WebApp webAppConf, SingletonProxyFactory singletonProxyFactory) {
        // 서버 활성화
        Spark.port(webAppConf.getPort());
        Spark.threadPool(webAppConf.getMaxConnection());
        Spark.staticFileLocation("/www");

        Spark.get("/node", "text/html", (req, res) -> {
            res.header("charset", "UTF-8");
            res.type("text/html");
            return IOUtils.toString(ClasspathUtil.getResourceAsStream("templates/index.html"));
        });

        restDefineFactory.forEach((key, value) -> {
            Object proxy = singletonProxyFactory.get(value.getComponentId());
            boolean isExistMethod;

            try {
                if (value.getMethodArgs() != null && value.getMethodArgs().length > 0) {
                    isExistMethod = proxy.getClass().getDeclaredMethod(value.getMethodName(),
                            Stream.of(value.getMethodArgs())
                                    .map(RestDefine.MethodArgs::getType)
                                    .collect(Collectors.toList())
                                    .toArray(new Class[0])) != null;
                } else {
                    isExistMethod = proxy.getClass().getDeclaredMethod(value.getMethodName()) != null;
                }
            } catch (NoSuchMethodException e) {
                isExistMethod = false;
                log.error(e.toString(), e);
            }

            String jsonAccept = HttpProvider.ContentType.JSON.getMimeType();
            try {
                switch (value.getAccept()) {
                    case HEAD:
                        final boolean finalIsExistMethod = isExistMethod;
                        Spark.head(key, (req, res) -> {
                            res.status(finalIsExistMethod ? 200 : 404);
                            return "";
                        });
                        break;
                    case PUT:
                        Spark.put(key, jsonAccept, _route(proxy, value), _toJson());
                        break;
                    case POST:
                        Spark.post(key, jsonAccept, _route(proxy, value), _toJson());
                        break;
                    case GET:
                        Spark.get(key, jsonAccept, _route(proxy, value), _toJson());
                        break;
                    case DELETE:
                        Spark.delete(key, jsonAccept, _route(proxy, value), _toJson());
                        break;
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
        });
    }

    private void _initCluster(ClusterContainer clusterContainer) {
        if (clusterContainer.valid()) {
            Set<Map.Entry<String, SharedMap>> clusterData = clusterContainer.getFactory().entrySet();
            for (Map.Entry<String, SharedMap> data : clusterData) {
                Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
                Spark.get(data.getKey(), "application/json", (res, req) -> {
                    SharedMap v = data.getValue();
                    log.info("{}", v);
                    return v;
                }, gson::toJson);
            }
        } else {
            log.warn("Cluster is not available.");
        }
    }


    private void _initLocalConf(Configuration globalConf, ComponentPolicyFactory policyFactory) {
        SharedMap data = new SharedMap();
        try {
            data.setAddress(InetAddress.getLocalHost().toString());
        } catch (UnknownHostException e) {
            log.error(e.toString(), e);
        }
        data.setArgs(globalConf.getVmArgs());
        data.setData(policyFactory);
        data.setConf(globalConf.getNodeConfig());

        Spark.get(globalConf.name(), "application/json", (res, req) -> {
            log.info("{}", data);
            return data;
        }, _toJson());
    }


    /**
     * Find RestBinder methods when HTTP request reached.
     *
     * @param proxy
     * @param value
     * @return
     */
    private Route _route(final Object proxy, final RestDefine value) {
        return ((req, res) -> {
            Object result;
            try {
                Method invokeTarget = methodMap.get(value.getUrl());
                _printRequestLog(req, proxy, invokeTarget);

                if (value.getMethodArgs() != null && value.getMethodArgs().length > 0) {
                    RestDefine.MethodArgs[] methodArgs = value.getMethodArgs();
                    Object[] args = _getMethodArgs(methodArgs, req);
                    result = _invoke(proxy, invokeTarget, args);
                } else {
                    result = _invoke(proxy, invokeTarget);
                }

                if (result instanceof String) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("result", (String) result);
                    result = jsonObject;
                }
                log.debug("Method result={}", result);
                return result;

            } catch (Throwable e) {
                log.error(e.toString(), e);
                return new ErrorResponse(e);
            }
        });
    }


    private Object _invoke(Object proxy, Method invokeTarget, Object... args) throws InvocationTargetException, IllegalAccessException {
        Object result;
        result = invokeTarget.invoke(proxy, args);

        return result != null ? result : "OK";
    }


    /**
     * JSON request로 인자를 받을 경우:
     * Query String으로 인자를 받을 경우: 1개의 파라미터로 선언된 이름이 중복될 경우, 최초의 1개만 인식함
     *
     * @param values
     * @param req
     * @return
     * @throws Exception
     */
    private Object[] _getMethodArgs(RestDefine.MethodArgs[] values, Request req) throws Exception {
        Object[] args = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            RestDefine.MethodArgs arg = values[i];
            String argName = arg.getName();
            Class<?> argType = arg.getType();

            Object reqParam;
            if (req.body() != null && req.body().length() > 0 && req.body().startsWith("{") && req.body().endsWith("}")) {
                // json body
                JsonParser parser = new JsonParser();
                JsonObject jsonBody = parser.parse(req.body()).getAsJsonObject();
                JsonElement jsonArg = jsonBody.get(argName);

                if (argType.isPrimitive()) {
                    reqParam = _getAsObject(argType, jsonArg);
                } else {
                    // map, collection, array, object
                    if (jsonArg.isJsonArray()) {
                        // json array
                        JsonArray array = jsonArg.getAsJsonArray();
                        Collection reqParamList;

                        if (List.class.isAssignableFrom(argType)) {
                            reqParamList = new ArrayList<>();
                        } else {
                            reqParamList = new HashSet<>();
                        }

                        for (JsonElement e : array) {
                            Object result = _getAsObject(arg.getGenericTypeNames() != null ? Class.forName(arg.getGenericTypeNames()[0]) : String.class, e);
                            reqParamList.add(result);
                        }
                        reqParam = reqParamList;

                    } else {
                        // json object
                        JsonObject object = jsonArg.getAsJsonObject();
                        if (arg.getTypeAdapter() != null) {
                            reqParam = new GsonBuilder().registerTypeAdapter(argType, arg.getTypeAdapter()).create().fromJson(object, argType);
                        } else {
                            reqParam = new Gson().fromJson(object, argType);
                        }
                    }
                }
            } else {
                // query param
                QueryParamsMap requestParam = req.queryMap().get(argName);
                reqParam = requestParam.value();
            }
            args[i] = reqParam;
        }

        return args;
    }


    private void _printRequestLog(Request req, Object proxy, Method invokeTarget) {
        if (log.isDebugEnabled()) {
            QueryParamsMap paramsMap = req.queryMap();

            StringBuilder paramBuilder = new StringBuilder();
            if (paramsMap.hasValue()) {
                paramBuilder.append("(");
                Map<String, String[]> queryParams = paramsMap.toMap();
                queryParams.forEach((k, v) ->
                        paramBuilder.append(k).append(":[")
                                .append(Stream.of(v).collect(Collectors.joining(","))).append("],"));
                paramBuilder.deleteCharAt(paramBuilder.length() - 1).append(")");
            }
            log.debug("{}#{} got request. query={}, body={}", proxy.getClass().getName(), invokeTarget.getName(), paramBuilder.length() > 0
                    ? paramBuilder : "NONE", !req.body().isEmpty() ? req.body() : "NONE");
        } else {
            log.info("{} got request.", proxy.getClass().getName());
        }
    }


    private ResponseTransformer _toJson() {
        return (model -> {
            Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
            try {
                return gson.toJson(model);
            } catch (Throwable e) {
                log.error(e.toString(), e);
                return gson.toJson(new ErrorResponse(e));
            }
        });
    }

    @Override
    public boolean valid() {
        return valid;
    }

    private Object _getAsObject(Class<?> argType, JsonElement jsonArg) {
        Object result;
        if (argType.isAssignableFrom(int.class) || argType.isAssignableFrom(Integer.class)) {
            result = jsonArg.getAsInt();
        } else if (argType.isAssignableFrom(long.class) || argType.isAssignableFrom(Long.class)) {
            result = jsonArg.getAsLong();
        } else if (argType.isAssignableFrom(float.class) || argType.isAssignableFrom(Float.class)) {
            result = jsonArg.getAsFloat();
        } else if (argType.isAssignableFrom(double.class) || argType.isAssignableFrom(Double.class)) {
            result = jsonArg.getAsDouble();
        } else if (argType.isAssignableFrom(char.class) || argType.isAssignableFrom(Character.class)) {
            result = jsonArg.getAsCharacter();
        } else if (argType.isAssignableFrom(boolean.class) || argType.isAssignableFrom(Boolean.class)) {
            result = jsonArg.getAsBoolean();
        } else if (argType.isAssignableFrom(byte.class) || argType.isAssignableFrom(Byte.class)) {
            result = jsonArg.getAsByte();
        } else if (argType.isAssignableFrom(short.class) || argType.isAssignableFrom(Short.class)) {
            result = jsonArg.getAsShort();
        } else {
            result = jsonArg.getAsString();
        }
        return result;
    }
}

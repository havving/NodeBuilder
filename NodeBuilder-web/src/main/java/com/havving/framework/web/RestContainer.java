package com.havving.framework.web;

import com.havving.framework.NodeBuilder;
import com.havving.framework.NodeContext;
import com.havving.framework.annotation.RestBinder;
import com.havving.framework.components.ComponentPolicyFactory;
import com.havving.framework.components.Container;
import com.havving.framework.components.SingletonProxyFactory;
import com.havving.framework.config.extensions.WebApp;
import com.havving.framework.domain.Configuration;
import com.havving.framework.exception.ContainerInitializeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassPathUtils;
import spark.Route;
import spark.Spark;
import spark.utils.IOUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
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
//            _initCluster((clusterContainer) clusterContainer);
        } else {
            _initLocalConf(globalConf, context.getPolicyFactory());
        }

        return null;
    }


    private void _initServer(WebApp webAppConf, SingletonProxyFactory singletonProxyFactory) {
        // 서버 활성화
        Spark.port(webAppConf.getPort());
        Spark.threadPool(webAppConf.getMaxConnection());
        Spark.staticFileLocation("/www");

/*        Spark.get("/node", "text/html", (req, res) -> {
            res.header("charset", "UTF-8");
            res.type("text/html");*/
            //TODO
//        });
    }

/*    private void _initCluster(ClusterContainer clusterContainer) {

    }*/


    private void _initLocalConf(Configuration globalConf, ComponentPolicyFactory policyFactory) {

    }


    @Override
    public boolean valid() {
        return valid;
    }
}

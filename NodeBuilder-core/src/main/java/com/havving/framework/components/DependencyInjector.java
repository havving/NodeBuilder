package com.havving.framework.components;

import com.havving.framework.annotation.Component;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 객체 정의를 받아 정책에 맞는 proxy를 생성하고 factory에 저장하는 클래스
 *
 * @author HAVVING
 * @since 2021-04-20
 */
@Slf4j
public class DependencyInjector {
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private Class<?> clz;
    @Setter
    private Set<String> instantIds;
    @Getter
    private Map<String, FieldMapping> mappingInfo;
    @Getter
    @Setter
    private String[] others;

    DependencyInjector() {
        this.mappingInfo = new HashMap<>();
    }

    public void createInstant(ComponentPolicyFactory policies) {
        _buildInjectorInfo(policies);
    }

    public Object createSingleton(SingletonProxyFactory factory, ComponentPolicyFactory policies) {
        Object componentProxy = factory.get(id) != null ? factory.get(id) : _createProxy(null, new MethodExecutionProxy(factory, instantIds, clz.getDeclaredAnnotation(Component.class).methodTracing()));
        _buildInjectorInfo(policies);

        return componentProxy;
    }


    private void _buildInjectorInfo(ComponentPolicyFactory policies) {
        BindPolicy policy = policies.getPolicy(id);
        PolicyDefine policyDefine = policies.getDefine(policy, id);
        Stream.of(policyDefine.getClz().getDeclaredFields())
                .filter(ComponentDependencyResolver::isBindableField)
                .forEach(f -> {
                    FieldMapping mapping = new FieldMapping();
                    mapping.className = f.getType().getTypeName();
                    mapping.name = f.getName();
                    mapping.type = policy.toString();
                    mapping.others = f.getType().getAnnotation(Component.class).other();
                    String id = f.getType().getAnnotation(Component.class).name();
                    mappingInfo.put(id, mapping);
                });
    }


    private Object _createProxy(CallbackFilter filter, MethodInterceptor... interceptor) {
        Enhancer result = new Enhancer();
        Class<?>[] refClasses = clz.getClasses();
        Class<?>[] refInterfaces = Stream.of(refClasses).filter(Class::isInterface).toArray(Class<?>[]::new);

        result.setInterfaces(refInterfaces);
        result.setSuperclass(clz);
        result.setCallbacks(interceptor);

        if (filter != null && interceptor.length > 1) {
            result.setCallbackFilter(filter);
        }

        return result.create();
    }

    public static class FieldMapping {
        @Getter
        String className;
        @Getter
        String name;
        @Getter
        String type;
        @Getter
        String[] others;
    }


    private static class MethodExecutionProxy implements MethodInterceptor {
        private Set<String> instants;
        private boolean methodTrace;
        private SingletonProxyFactory singletonProxyFactory;

        MethodExecutionProxy(SingletonProxyFactory singletonProxyFactory, Set<String> instants, boolean methodTrace) {
            this.singletonProxyFactory = singletonProxyFactory;
            this.instants = instants;
            this.methodTrace = methodTrace;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            return null;
        }
    }
}

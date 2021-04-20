package com.havving.framework.components;

import com.havving.framework.annotation.Component;
import com.havving.framework.exception.ContainerInitializeException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author HAVVING
 * @since 2021-04-19
 */
@Slf4j
public class ComponentContainer implements Container<SingletonProxyFactory> {
    @Getter
    private Map<String, DependencyInjector> injectorFactory;    // 각 객체들 간의 D/I 정의
    @Getter
    private ComponentPolicyFactory policyFactory;   // proxy 객체들의 life-cycle 정책
    private SingletonProxyFactory proxyFactory;  // proxy 객체들을 저장
    private boolean valid = false;

    @Override
    public CoreExtensions getExtensionType() {
        return null;
    }

    @Override
    public SingletonProxyFactory getFactory() {
        return null;
    }

    @Override
    public synchronized ComponentContainer initializeToScan(String scanPackage) throws ContainerInitializeException {
        try {
            Set<Class<?>> componentClasses = null;

            if (scanPackage.equals("com.havving.framework")) {
                log.warn("com.havving.framework package will scan automatically.");
            } else {
                log.info("package {} scanning start.", scanPackage);
                Reflections ref = new Reflections(scanPackage);  // scanPackage 아래의 모든 객체를 검색한다.
                componentClasses = ref.getTypesAnnotatedWith(Component.class);  // @Component가 붙은 모든 클래스를 검색한다.
            }

            // com.havving.framework 패키지 아래 @Component가 붙은 모든 클래스를 검색한다.
            Set<Class<?>> moduleClasses = new Reflections("com.havving.framework").getTypesAnnotatedWith(Component.class);

            policyFactory = new ComponentPolicyFactory();   // singleton, instance

            // default module append
            if (componentClasses != null) {
                moduleClasses.addAll(componentClasses);
            }

            moduleClasses.forEach(e -> {
                Component managerAnno = e.getDeclaredAnnotation(Component.class);   // @Component Annotation을 구한다.
                policyFactory.put(e, new PolicyDefine(managerAnno.name(), e));  // Component 객체에 @Component 클래스를 담는다.
            });

            ComponentDependencyResolver resolver = new ComponentDependencyResolver();   // @Component 내 @Bind 필드 정의 객체를 생성한다.
            resolver.resolve(policyFactory);    // 계산이 완료된 D/I 객체를 저장한다.

            proxyFactory = new SingletonProxyFactory();  // Component 객체의 proxy를 저장하는 객체를 생성한다.
            injectorFactory = this._inject(resolver.getValues());
            valid = true;

            return this;
        } catch (Throwable e) {
            throw new ContainerInitializeException(e);
        }
    }


    /**
     * 실제 D/I 수행 메서드
     *
     * @param idValues D/I 정의들
     * @return D/I 수행 후 완료된 D/I 정의 객체를 반환
     */
    private Map<String, DependencyInjector> _inject(Set<ComponentDependencyResolver.DependencyIdMaps> idValues) {
        Map<String, DependencyInjector> injectorMap = new HashMap<>(idValues.size());

        idValues.forEach(e -> {
            Set<String> singleIds = e.getSingleIds();
            Set<String> instantIds = e.getInstantIds();

            if (singleIds == null)
                singleIds = Collections.EMPTY_SET;
            if (instantIds == null)
                instantIds = Collections.EMPTY_SET;

            DependencyInjector injector = new DependencyInjector();
            injector.setId(e.getId());
            injector.setClz(e.getClz());
            injector.setInstantIds(instantIds);

            if (e.getClz().getDeclaredAnnotation(Component.class).singleton()) {
                Object result = injector.createSingleton(proxyFactory, policyFactory);
                String[] others = e.getClz().getDeclaredAnnotation(Component.class).other();

                if (others.length > 0) {
                    injector.setOthers(others);
                    for (int i = 0; i < others.length; i++) {
                        DependencyInjector subInjector = new DependencyInjector();
                        subInjector.setId(others[i]);
                        subInjector.setClz(e.getClz());
                        subInjector.setInstantIds(instantIds);
                        Object subResult = subInjector.createSingleton(proxyFactory, policyFactory);
                        proxyFactory.put(others[i], subResult);
                    }
                }
                proxyFactory.put(e.getId(), result);
                log.debug("Singleton object created {}", result.getClass().getName());
            } else {
                injector.createInstant(policyFactory);
            }
            injectorMap.put(e.getId(), injector);
        });

        return injectorMap;
    }


    @Override
    public boolean valid() {
        return valid;
    }

}

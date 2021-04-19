package com.havving.framework.components;

import com.havving.framework.annotation.Component;
import com.havving.framework.exception.ContainerInitializeException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Set;

/**
 * @author HAVVING
 * @since 2021-04-19
 */
@Slf4j
public class ComponentContainer implements Container<SingletonProxyFactory> {
    private boolean valid = false;
    @Getter
    private ComponentPolicyFactory policyFactory;


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
                Reflections ref = new Reflections(scanPackage);
                componentClasses = ref.getTypesAnnotatedWith(Component.class);
            }

            Set<Class<?>> moduleClasses = new Reflections("com.havving.framework").getTypesAnnotatedWith(Component.class);

            policyFactory = new ComponentPolicyFactory();

            if (componentClasses != null) {
                moduleClasses.addAll(componentClasses);
            }

            moduleClasses.forEach(e -> {
                Component managerAnno = e.getDeclaredAnnotation(Component.class);
                policyFactory.put(e, new PolicyDefine(managerAnno.name(), e));
            });

            ComponentDependencyResolver resolver = new ComponentDependencyResolver();
            resolver.resolve(policyFactory);


            return this;
        } catch (Throwable e) {
            throw new ContainerInitializeException(e);
        }
    }


    @Override
    public boolean valid() {
        return valid;
    }

}

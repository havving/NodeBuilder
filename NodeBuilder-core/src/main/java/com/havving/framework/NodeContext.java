package com.havving.framework;

import com.havving.framework.components.ComponentContainer;
import com.havving.framework.components.Container;
import com.havving.framework.exception.ContainerInitializeException;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.Map;

import static com.havving.framework.components.Container.CoreExtensions.DEFAULT;

/**
 * 설정 정보 및 각 모듈이 등록되는 클래스
 *
 * @author HAVVING
 * @since 2021-04-19
 */
@Slf4j
public class NodeContext {
    private static final NodeContext context = new NodeContext();
    private final Map<Container.CoreExtensions, Container> containers;

    private NodeContext() {
        this.containers = new EnumMap<>(Container.CoreExtensions.class);
        Thread DEFAULT_SHUTDOWN = new Thread(() -> log.info("Node goes down."));
        Runtime.getRuntime().addShutdownHook(DEFAULT_SHUTDOWN);
    }

    static NodeContext getInstance() {
        if (context.containers.get(DEFAULT) != null && !context.containers.get(DEFAULT).valid())
            log.warn("Scan package didn't apply yet. Please run init method.");
        return context;
    }

    public void init(String scanPackage) throws ContainerInitializeException {
        this.containers.put(DEFAULT, new ComponentContainer().initializeToScan(scanPackage));
    }
}

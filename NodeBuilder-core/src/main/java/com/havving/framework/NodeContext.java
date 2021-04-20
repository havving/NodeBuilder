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
    private static final NodeContext context = new NodeContext();   // 한 프로세스 내에 단일 context만 생성토록 함
    private final Map<Container.CoreExtensions, Container> containers;  // Container 저장 객체

    private NodeContext() {
        this.containers = new EnumMap<>(Container.CoreExtensions.class);
        Thread DEFAULT_SHUTDOWN = new Thread(() -> log.info("Node goes down."));
        Runtime.getRuntime().addShutdownHook(DEFAULT_SHUTDOWN);
    }

    /**
     * singleton 방식으로 NodeContext return
     *
     * @return 단일 생성 된 context 객체
     */
    static NodeContext getInstance() {
        if (context.containers.get(DEFAULT) != null && !context.containers.get(DEFAULT).valid())
            log.warn("Scan package didn't apply yet. Please run init method.");
        return context;
    }


    /**
     * 기본 core 생성 및 등록
     *
     * @param scanPackage @Component를 스캔할 패키지명
     * @see com.havving.framework.annotation.Component
     * @throws ContainerInitializeException
     */
    public void init(String scanPackage) throws ContainerInitializeException {
        this.containers.put(DEFAULT, new ComponentContainer().initializeToScan(scanPackage));
    }
}

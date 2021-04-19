package com.havving.framework.components;

import com.havving.framework.exception.ContainerInitializeException;

import java.util.Map;

/**
 * Context에 등록되는 Container의 기본 인터페이스
 *
 * @author HAVVING
 * @since 2021-04-19
 */
public interface Container<T extends Map> {

    /**
     * 해당 Container의 타입을 반환
     * @return
     */
    CoreExtensions getExtensionType();


    /**
     * Container의 기본 factory 반환
     * @return
     */
    T getFactory();


    /**
     * 생성 후 실제 init 수행
     * @param scanPackage
     * @return
     * @throws ContainerInitializeException
     */
    Container initializeToScan(String scanPackage) throws ContainerInitializeException;


    /**
     * init이 제대로 완료되었는지 검증
     * @return
     */
    boolean valid();

    enum CoreExtensions {
        DEFAULT, CLUSTER, WEB, REPL, JMX, RV, STORE, SCHEDULER
    }
}

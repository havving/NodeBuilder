package com.havving.framework;

import com.havving.framework.components.Container;
import com.havving.framework.exception.ContainerInitializeException;

/**
 * Core에 모듈로 등록될 객체는 이 인터페이스를 상속받아야 하고,
 * CoreExtensions에 신규 필드를 등록해야 함
 *
 * @author HAVVING
 * @since 2021-04-23
 * @see com.havving.framework.components.Container.CoreExtensions
 */
public interface Extension<T extends Container> {
    /**
     * Container 활성화
     * <p>
     *     ex)
     *     <pre>
     *     public Container activate() {
     *         return new Container().initializeToScan(scanPackage);
     *     }
     *     </pre>
     * </p>
     *
     * @return initializeToScan 메서드가 수행된 Container 객체
     * @throws ContainerInitializeException
     */
    T activate() throws ContainerInitializeException;

    /**
     * init 가능한 Container인지 사전 체크
     *
     * @return t of f
     */
    boolean initializable();
}

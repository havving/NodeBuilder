package com.havving.framework.scheduler;

import com.havving.framework.Extension;
import com.havving.framework.exception.ContainerInitializeException;

/**
 * Scheduler 모듈 활성화 객체
 *
 * @author HAVVING
 * @since 2021-05-05
 */
public class NodeSchedulerExtension implements Extension<SchedulerContainer> {

    @Override
    public SchedulerContainer activate() throws ContainerInitializeException {
        return null;
    }

    @Override
    public boolean initializable() {
        return false;
    }
}

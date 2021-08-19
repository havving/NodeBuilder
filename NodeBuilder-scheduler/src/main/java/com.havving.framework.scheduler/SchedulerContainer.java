package com.havving.framework.scheduler;

import com.havving.framework.components.Container;
import com.havving.framework.exception.ContainerInitializeException;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import java.util.Map;

/**
 * Scheduler 기능 수행 class
 *
 * @author HAVVING
 * @since 2021-05-05
 */
public class SchedulerContainer implements Container<Map<String, SchedulerContainer.JobAndTriggerSet>> {


    @Override
    public CoreExtensions getExtensionType() {
        return null;
    }

    @Override
    public Map<String, JobAndTriggerSet> getFactory() {
        return null;
    }

    @Override
    public Container initializeToScan(String scanPackage) throws ContainerInitializeException {
        return null;
    }

    @Override
    public boolean valid() {
        return false;
    }

    static class JobAndTriggerSet {
        JobDetail jobDetail;
        Trigger trigger;

        JobAndTriggerSet(JobDetail jobDetail, Trigger trigger) {
            this.jobDetail = jobDetail;
            this.trigger = trigger;
        }
    }
}

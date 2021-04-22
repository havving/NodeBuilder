package com.havving.framework.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author HAVVING
 * @since 2021-04-22
 */
@Data
@AllArgsConstructor
public class VmStat {
    private final boolean success;
    private final String msg;
    private final String osName;
    private final String version;
    private final int processors;
    private final double loadAvg;
    private final double sysCpuLoad;
    private final double procCpuLoad;
    private final int totalThreads;
    private final int totalDaemonThreads;
    private final long[] deadLocks;
    private final long freePhysicalMemSize;
    private final long totalPhysicalMemSize;
    private final long freeSwapSize;
    private final long totalSwapSize;
    private final String compiler;
    private final long committedVMsize;
    private final long heapInit;
    private final long heapCommitted;
    private final long heapMax;
    private final long heapUsed;
    private final long nonHeapInit;
    private final long nonHeapCommitted;
    private final long nonHeapMax;
    private final long nonHeapUsed;

    private VmStat(Exception e) {
        this.success = false;
        String msg = e.getMessage();
        if (msg == null || msg.length() < 1) {
            this.msg = e.getClass().getName();
        } else {
            this.msg = msg;
        }
        compiler = StringUtils.EMPTY;
        heapInit = -1;
        heapCommitted = -1;
        heapMax = -1;
        heapUsed = -1;
        nonHeapInit = -1;
        nonHeapCommitted = -1;
        nonHeapMax = -1;
        nonHeapUsed = -1;
        osName = StringUtils.EMPTY;
        version = StringUtils.EMPTY;
        processors = -1;
        loadAvg = -1;
        committedVMsize = -1;
        totalSwapSize = -1;
        freeSwapSize = -1;
        freePhysicalMemSize = -1;
        totalPhysicalMemSize = -1;
        sysCpuLoad = -1;
        procCpuLoad = -1;
        totalThreads = -1;
        totalDaemonThreads = -1;
        deadLocks = new long[0];
    }

    public static VmStat ERROR(Exception e) {
        return new VmStat(e);
    }

    public static VmStatBuilder builder() {
        return new VmStatBuilder();
    }
}

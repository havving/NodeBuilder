package com.havving.framework.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author HAVVING
 * @since 2021-04-22
 */
@Data
@Accessors(fluent = true, chain = true)
public final class VmStatBuilder {
    private boolean success;
    private String msg;
    private String compiler;
    private long heapInit;
    private long heapCommitted;
    private long heapMax;
    private long heapUsed;
    private long nonHeapInit;
    private long nonHeapCommitted;
    private long nonHeapMax;
    private long nonHeapUsed;
    private String osName;
    private String version;
    private int processors;
    private double loadAvg;
    private long committedVMsize;
    private long totalSwapSize;
    private long freeSwapSize;
    private long freePhysicalMemSize;
    private long totalPhysicalMemSize;
    private double sysCpuLoad;
    private double procCpuLoad;
    private int totalThreads;
    private int totalDaemonThreads;
    private long[] deadLocks;

    public VmStat build() {
        return new VmStat(success, msg, osName, version, processors, loadAvg, sysCpuLoad, procCpuLoad, totalThreads, totalDaemonThreads, deadLocks,
                freePhysicalMemSize, totalPhysicalMemSize, freeSwapSize, totalSwapSize, compiler, committedVMsize, heapInit, heapCommitted, heapMax,
                heapUsed, nonHeapInit, nonHeapCommitted, nonHeapMax, nonHeapUsed);
    }
}

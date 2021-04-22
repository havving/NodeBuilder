package com.havving.framework;

import com.havving.framework.annotation.Shared;
import com.havving.framework.components.ComponentContainer;
import com.havving.framework.components.Container;
import com.havving.framework.domain.Configuration;
import com.havving.framework.domain.VmStat;
import com.havving.framework.domain.VmStatBuilder;
import com.havving.framework.exception.ContainerInitializeException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.*;
import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

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
    @Getter @Setter
    @Shared(id = "GLOBAL", lookup = true)
    private volatile Configuration config;  // 설정 정보. NodeContext는 Component가 아니라 스캔은 되지 않지만, 개념상 부여함
    private static final Semaphore MUTEX = new Semaphore(1);
    private static Callable<VmStat> vmCollectWorker;
    private static WeakReference<VmStat> vmStatCache;


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

    public static void activateVmStatCollector() {
        try {
            MUTEX.acquire();
            vmCollectWorker = () -> {
                VmStatBuilder stat = VmStat.builder();
                CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
                stat.compiler(compilationMXBean.getName());

                MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
                MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
                stat.heapInit(heapUsage.getInit()).heapCommitted(heapUsage.getCommitted()).heapMax(heapUsage.getMax()).heapUsed(heapUsage.getUsed());

                MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
                stat.nonHeapInit(nonHeapUsage.getInit()).nonHeapCommitted(nonHeapUsage.getCommitted()).nonHeapMax(nonHeapUsage.getMax()).nonHeapUsed(nonHeapUsage.getUsed());

                if (System.getProperty("java.vendor") != null && !System.getProperty("java.vendor").toLowerCase().contains("oracle")) {
                    OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
                    stat.osName(operatingSystemMXBean.getName()).version(operatingSystemMXBean.getVersion()).processors(operatingSystemMXBean.getAvailableProcessors())
                            .loadAvg(operatingSystemMXBean.getSystemLoadAverage());

                    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                    stat.totalThreads(threadMXBean.getThreadCount()).totalDaemonThreads(threadMXBean.getDaemonThreadCount()).deadLocks(threadMXBean.findDeadlockedThreads());
                } else {
                    com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                    stat.osName(operatingSystemMXBean.getName()).version(operatingSystemMXBean.getVersion()).processors(operatingSystemMXBean.getAvailableProcessors())
                            .loadAvg(operatingSystemMXBean.getSystemLoadAverage())
                            .committedVMsize(operatingSystemMXBean.getCommittedVirtualMemorySize()).totalSwapSize(operatingSystemMXBean.getTotalSwapSpaceSize())
                            .freeSwapSize(operatingSystemMXBean.getFreeSwapSpaceSize()).freePhysicalMemSize(operatingSystemMXBean.getFreePhysicalMemorySize())
                            .totalPhysicalMemSize(operatingSystemMXBean.getTotalPhysicalMemorySize()).sysCpuLoad(operatingSystemMXBean.getSystemCpuLoad())
                            .procCpuLoad(operatingSystemMXBean.getProcessCpuLoad());

                    com.sun.management.ThreadMXBean threadMXBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
                    stat.totalThreads(threadMXBean.getThreadCount()).totalDaemonThreads(threadMXBean.getDaemonThreadCount()).deadLocks(threadMXBean.findDeadlockedThreads());
                }
                return stat.success(true).build();
            };

            try {
                vmStatCache = new WeakReference<>(vmCollectWorker.call());
            } catch (Exception e) {
                log.error(e.toString(), e);
            }

        } catch (InterruptedException e) {
            log.error(e.toString(), e);
        } finally {
            MUTEX.release();
        }
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

package com.havving.framework;

import com.havving.framework.annotation.Shared;
import com.havving.framework.components.ComponentContainer;
import com.havving.framework.components.ComponentPolicyFactory;
import com.havving.framework.components.Container;
import com.havving.framework.domain.Configuration;
import com.havving.framework.domain.VmStat;
import com.havving.framework.domain.VmStatBuilder;
import com.havving.framework.exception.ContainerInitializeException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.management.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

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
     */
    public void init(String scanPackage) throws ContainerInitializeException {
        this.containers.put(DEFAULT, new ComponentContainer().initializeToScan(scanPackage));
    }


    /**
     * Extension 인터페이스를 상속받은 모듈 객체를 활성화하여 Container로 등록
     *
     * @see com.havving.framework.Extension
     * @see com.havving.framework.components.Container
     */
    public void activateExtensions() throws ContainerInitializeException {
        Set<Class<? extends Extension>> extensions = new Reflections("com.havving").getSubTypesOf(Extension.class);
        List<Class<? extends Extension>> sortedExtension = extensions.stream().sorted((e1, e2) ->
                e1.getSimpleName().equals("NodeClusterExtension") ? -1 : 0
        ).collect(Collectors.toList());

        for (Class<? extends Extension> e : sortedExtension) {
            try {
                log.info("{} has been found. Extension activate starts.", e.getName());
                Constructor constructor = e.getConstructor();
                if (constructor == null) {
                    log.error("NodeBuilder couldn't find Extension's default constructor.");
                    throw new ContainerInitializeException("NodeBuilder couldn't find Extension's default constructor.");
                }

                Extension extension = e.newInstance();
                if (extension.initializable()) {
                    addContainer(extension.activate());
                    log.info("{} init complete.", e.getName());
                }

            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | ContainerInitializeException e1) {
                throw new ContainerInitializeException(e1);
            }
        }
    }


    /**
     * Container 객체 등록 후 재사용 할 수 있도록 리턴
     *
     * @param container Container 인터페이스 상속 객체
     * @return 등록된 Container 객체
     * @see com.havving.framework.components.Container
     */
    public Container addContainer(Container container) {
        Container.CoreExtensions type = container.getExtensionType();
        if (containers.containsKey(type)) {
            log.warn("Component container type {} already exists. Component will be updated.", type);
            log.debug("\t\twas : {}", containers.get(type));
            log.debug("\t\t be : {}", container);
        }
        this.containers.put(container.getExtensionType(), container);

        return container;
    }


    /**
     * Object Factory에 사용된 Life-Cycle 정책을 내장한 Factory를 반환
     *
     * @return Life-Cycle 정책 Factory
     * @see com.havving.framework.components.ComponentPolicyFactory
     */
    public ComponentPolicyFactory getPolicyFactory() {
        return ((ComponentContainer) this.containers.get(DEFAULT)).getPolicyFactory();
    }
}

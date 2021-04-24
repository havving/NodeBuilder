package com.havving.framework;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import com.havving.framework.components.BindPolicy;
import com.havving.framework.config.*;
import com.havving.framework.domain.Configuration;
import com.havving.framework.domain.JvmGcData;
import com.havving.framework.domain.JvmGcData.GcMemoryData;
import com.havving.framework.exception.ContainerInitializeException;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.io.File;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.havving.framework.config.InitArguments.*;

/**
 * NodeBuilder가 실행될 Main Class 및 End-Point Object
 *
 * @author HAVVING
 * @since 2021-04-16
 */
@Slf4j
public class NodeBuilder {
    private static final String NODE_LOGGER = "node-logger";
    private static final ThreadLocal<VmDataHandlingWorker> gcDataHandlingWorker = new ThreadLocal<VmDataHandlingWorker>();
    private static volatile boolean GC_HANDLER_REGISTERED = false;
    /**
     * 기동 시 생성되는 실제 context 객체
     * 기동 시, 설정 정보 등 NodeBuilder에서 관장하는 모든 데이터를 내장
     * NodeBuilder.getContext()로 호출 가능
     */
    private static NodeContext context;

    /**
     * 프로세스를 up 상태로 유지시켜줄 Daemon Thread
     * DAEMON 모드일 경우, 프로세스 기동과 함께 실행되며, NodeBuilder.exit() 메서드로 interrupted 발생
     * @see com.havving.framework.config.AppType
     */
    private static final ThreadLocal<Thread> daemon = ThreadLocal.withInitial(() -> new Thread("WAIT_DAEMON") {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    log.info("DAEMON thread interrupted. Node goes down.");
                    break;
                }
            }
        }
    });

    public NodeBuilder() {
    }

    /**
     * NodeBuilder 기동 시작점
     * VM에 등록된 설정 정보를 검증하고, 실제 설정 정보를 생성하여 _build 메서드를 호출하여 기동
     */
    public static void main(String[] args) {
        try {
            // APP Name check
            String appName = null;
            if (System.getProperty(APP_NAME.getKey()) != null) {
                appName = System.getProperty(APP_NAME.getKey());
                log.info("Application {} now running...", appName);
            } else {
                log.error("-D{} didn't set. Couldn't initialize node.", APP_NAME.getKey());
                System.exit(-1);
            }
            // Log Level check
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Level logLevel;
            String level = System.getProperty(LOG_LEVEL.getKey());
            logLevel = Level.toLevel(level, Level.DEBUG);  // level이 null일 경우, 두 번째 인자를 기본값으로 사용
            initLogFileAppender(loggerContext, appName, logLevel);

            // GC Lookup check
            if (System.getProperty(GC_LOOKUP.getKey()) != null) {
                log.info("GC logging enabled.");
                _initGCLogger();
            }

            Logger reflectionsLogger = loggerContext.getLogger("org.reflections");
            reflectionsLogger.setLevel(Level.ERROR);
            Logger httpLogger = loggerContext.getLogger("org.apache.httpcomponents");
            httpLogger.setLevel(Level.ERROR);

            NodeConfig config;

            // 기동 검색 위치 설정
            if (System.getProperty(SCAN_PKG.getKey()) != null) {
                config = new NodeConfig();
                config.setScanPackage(System.getProperty(SCAN_PKG.getKey()));
                if (System.getProperty(APP_TYPE.getKey()) == null) {   // node.scan을 사용하려면 node.app.type을 지정해야 함
                    log.warn(APP_TYPE.getKey() + " is null. This will run DAEMON mode.");
                    System.setProperty(APP_TYPE.getKey(), AppType.DAEMON.toString());   // DAEMON 모드 default 설정
                }
                config.setAppType(AppType.getType(System.getProperty(APP_TYPE.getKey())));
            } else {
                ResourceResolver resolver;
                String path;

                // node.scan == null
                if (System.getProperty(CONFIG_PATH.getKey()) != null) {   // node.path.conf (node-conf.xml 절대경로 위치 지정)
                    resolver = new AbsolutePathResourceResolver();   // 절대경로를 읽어 파일 deserializer
                    path = System.getProperty(CONFIG_PATH.getKey());
                    log.info("-D{} exists. Using config path '{}", CONFIG_PATH.getKey(), path);
                } else {    // node.path.conf, node.scan == null
                    resolver = new ClassPathResourceResolver();   // classpath 아래의 node-conf.xml 파일 검색
                    path = "classpath:node-conf.xml";
                    log.info("-D{} argument didn't found. node-conf.xml find start... '{}'", SCAN_PKG.getKey(), path);
                }

                try {
                    config = resolver.read(path);   // 파일 deserializer
                } catch (Exception e) {
                    config = null;
                    log.error(e.toString(), e);
                    log.error("node configuration file couldn't read. Process goes down.");
                    System.exit(-1);
                }
            }

            _build(appName, config, args);

            if (config.getAppType() == null || config.getAppType().equals(AppType.DAEMON)) {
                log.info("Node has been set DAEMON mode.");
                daemon.get().start();
            } else {
                log.info("Node app-type doesn't set. This process will be executing once.");
            }
            log.info("Node '" + appName + "' running success.");
            log.info("\n___________________________________________________________________________________________________________\n" +
                    "    _     _      __      _____     _____     ____      _     _     __    _       _____     _____     ____  \n" +
                    "    /|   /     /    )    /    )    /    '    /   )     /    /      /     /       /    )    /    '    /    )\n" +
                    "---/-| -/-----/----/----/----/----/__-------/__ /-----/----/------/-----/-------/----/----/__-------/___ /-\n" +
                    "  /  | /     /    /    /    /    /         /    )    /    /      /     /       /    /    /         /    |  \n" +
                    "_/___|/_____(____/____/____/____/____ ____/____/____(____/____ _/_ ___/____/__/____/____/____ ____/_____|__\n" +
                    "                                                                                                           \n" +
                    "___________________________________________________________________________________________________________\n");

            String pid;

            if (System.getProperty(PID_PATH.getKey()) != null) {
                String pidFilePath = System.getProperty(PID_PATH.getKey());
                pid = _createPidFile(pidFilePath + "/" + appName);
            } else {
                String name = ManagementFactory.getRuntimeMXBean().getName();
                pid = name.split("@")[0];
            }

            log.info("\n___________________________________________________________________________________________________________\n" +
                            "<<<<<<<<<<<<<< {} Node up and work starts." + "\n" +
                            "  <<<<<<<<<<<<    name : {}\n" +
                            "    <<<<<<<<<<    pid : {}\n" +
                            "      <<<<<<<<    components(singleton) : {}\n" +
                            "        <<<<<<    components(instant) : {}\n" +
                            "___________________________________________________________________________________________________________\n",
                    LocalDateTime.now(), appName, pid,
                    context.getPolicyFactory().get(BindPolicy.Singleton).size(),
                    context.getPolicyFactory().get(BindPolicy.Instant).size()
            );

        } catch (Exception e) {
            log.error(e.toString(), e);
            System.exit(-1);
        }

    }


    private static String _createPidFile(String appName) {
        try {
            File pidFile = new File(appName + ".pid");
            if (pidFile.exists()) {
                FileUtils.forceDelete(pidFile);
            }
            String name = ManagementFactory.getRuntimeMXBean().getName();
            String pid = name.split("@")[0];
            FileUtils.write(pidFile, pid, Charset.forName("UTF-8"), false);
            String logString = "Process ID file has been initialized. pid: " + pid + " locaction: " + pidFile.getAbsoluteFile();
            System.out.println(logString);
            log.info(logString);

            return pid;

        } catch (IOException e) {
            log.error("", e);

            return null;
        }
    }


    /**
     * 내부 빌드 메서드
     *
     * @param appName
     * @param config
     * @param args
     */
    private static void _build(String appName, NodeConfig config, String[] args) throws ContainerInitializeException {
        NodeContext context = NodeContext.getInstance();    // 인스턴스(한 번 사용하고 버림) 객체를 얻어,
        context.init(config.getScanPackage());  // config의 scanPakcage 정보를 가져와 기본 core를 생성하고 등록한다.

        Configuration configuration = Configuration.init(appName);  // node.app.name을 이용하여 고유의 Configuration 객체를 생성한다.
        configuration.apply(config);    // 외부 config를 가져와 내부 config에 update 한다.
        context.setConfig(configuration);   // configuration을 설정한다.
        if (System.getProperty(STAT_VM.getKey()) != null) {
            NodeContext.activateVmStatCollector();  // VM Stat을 수집한다.
        }
        NodeBuilder.context = context;  // 기동 시 생성되는 실제 context 객체에, 위에서 설정한 context 객체를 할당한다.
        context.activateExtensions();   // Extension 인터페이스를 상속받은 모듈 객체를 활성화하여 Container로 등록한다.
    }


    private static void initLogFileAppender(LoggerContext loggerContext, String appName, Level logLevel) {
        RollingFileAppender rfAppender = new RollingFileAppender();
        rfAppender.setName("NodeFileAppender");
        rfAppender.setContext(loggerContext);
        rfAppender.setFile("logs/node.log");
        rfAppender.setAppend(true);
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        // rolling policies need to know their parent
        // it's one of the rare cases, where a sub-component knows about its parent
        rollingPolicy.setMaxIndex(9);
        rollingPolicy.setMinIndex(0);
        rollingPolicy.setParent(rfAppender);
        rollingPolicy.setFileNamePattern("logs/node.%i.log");
        rollingPolicy.start();

        SizeBasedTriggeringPolicy triggeringPolicy = new ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy();
        triggeringPolicy.setMaxFileSize(FileSize.valueOf("10MB"));
        triggeringPolicy.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setCharset(Charset.forName("UTF-8"));
        encoder.setPattern("[%d{yyyy-MM-dd HH:mm:ss.SSS}][%thread] %msg \\(%file:%line\\)%n");
        encoder.start();

        rfAppender.setEncoder(encoder);
        rfAppender.setRollingPolicy(rollingPolicy);
        rfAppender.setTriggeringPolicy(triggeringPolicy);
        rfAppender.start();

        // attach the rolling file appender to the logger of your choice
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        Optional<ConsoleAppender> consoleAppender = Optional.empty();
        for (Iterator<Appender<ILoggingEvent>> it = rootLogger.iteratorForAppenders(); it.hasNext(); ) {
            Appender appender = it.next();
            if (ConsoleAppender.class.isAssignableFrom(appender.getClass())) {
                consoleAppender = Optional.of((ConsoleAppender) appender);
                break;
            }
        }
        Logger nodeLogger = loggerContext.getLogger(NODE_LOGGER);
        consoleAppender.ifPresent(nodeLogger::addAppender);
        nodeLogger.addAppender(rfAppender);
        nodeLogger.setAdditive(false);
        nodeLogger.setLevel(Level.TRACE);

        Logger frameworkLogger = loggerContext.getLogger("com.havving.framework");
        frameworkLogger.addAppender(rfAppender);
        frameworkLogger.setLevel(Level.TRACE);
    }


    private static void _initGCLogger() {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            NotificationEmitter emitter = (NotificationEmitter) gcBean;
            NotificationListener listener = new NotificationListener() {
                long totalGcDuration = 0;

                // implement the notifier callback handler
                @Override
                public void handleNotification(Notification notification, Object handback) {
                    // we only handle GARBAGE_COLLECTION_NOTIFICATION notifications here
                    if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                        // get the information associated with this notification
                        GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                        GcInfo gcInfo = info.getGcInfo();
                        // get all the info and pretty print it
                        long duration = gcInfo.getDuration();
                        String gcType = info.getGcAction();
                        log.warn("GC LOGGING ------------------------------");
                        log.warn("  INFO: ID={}, NAME={}, TYPE={} CAUSE={} occurred until {}. {} to {} milliseconds", gcInfo.getId(), info.getGcName(), gcType, info.getGcCause(), duration, gcInfo.getStartTime(), gcInfo.getEndTime());
                        log.warn("  CompositeType: {}", gcInfo.getCompositeType());
                        log.warn("  MemoryUsageAfterGc: {}", gcInfo.getMemoryUsageAfterGc());
                        log.warn("  MemoryUsageBeforeGc: {}", gcInfo.getMemoryUsageBeforeGc());

                        // get the informatiotn about each memory space, and pretty print it
                        Map<String, MemoryUsage> memBefore = gcInfo.getMemoryUsageBeforeGc();
                        Map<String, MemoryUsage> mem = gcInfo.getMemoryUsageAfterGc();
                        log.warn("  Memory Usage: {}", mem);
                        final JvmGcData gcData = new JvmGcData(gcInfo.getId(), info.getGcName(), gcType, info.getGcCause(), duration, gcInfo.getStartTime(), gcInfo.getEndTime());

                        for (Map.Entry<String, MemoryUsage> entry : mem.entrySet()) {
                            String name = entry.getKey();
                            MemoryUsage memDetail = entry.getValue();
                            long memInit = memDetail.getInit();
                            long memCommitted = memDetail.getCommitted();
                            long memMax = memDetail.getMax();
                            long memUsed = memDetail.getUsed();

                            MemoryUsage before = memBefore.get(name);
                            long beforePercent = (before.getUsed() * 1000L) / before.getCommitted();
                            long percent = (memUsed * 1000L) / before.getCommitted();  // >100% when it gets expended
                            log.warn("    {}({}): ", name, memCommitted == memMax ? "reached full memory" : "still expandable");
                            log.warn("        used: {}.{}% -> {}.{}%. (used={},init={} (MB))", beforePercent / 10, beforePercent % 10, percent / 10, percent % 10, (memUsed / 1048576) + 1, (memInit / 1048576) + 1);
                            if (GC_HANDLER_REGISTERED) {
                                GcMemoryData memoryData = new GcMemoryData(memInit, memCommitted, memMax, memUsed, ((before.getUsed() * 1000L) / before.getCommitted()), ((memUsed * 1000L) / before.getCommitted()));
                                gcData.getMemData().put(name, memoryData);
                            }
                        }
                        totalGcDuration += gcInfo.getDuration();
                        long percent = totalGcDuration * 1000L / gcInfo.getEndTime();
                        log.warn("  overhead: " + (percent / 10) + "." + (percent % 10) + "%");
                        log.warn("------------------------------ GC LOGGING");
                        if (GC_HANDLER_REGISTERED) {
                            log.info("GC Handling worker execute.");
                            CompletableFuture.supplyAsync(() -> gcDataHandlingWorker.get().handle(gcData));
                        }
                    }

                }
            };
            // Add the listener
            emitter.addNotificationListener(listener, null, null);
        }
    }

    @FunctionalInterface
    public interface VmDataHandlingWorker {
        JvmGcData handle(JvmGcData gcData);
    }
}

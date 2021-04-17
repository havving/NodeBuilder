package com.havving.framework;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import com.havving.framework.domain.JvmGcData;
import com.havving.framework.domain.JvmGcData.GcMemoryData;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.charset.Charset;
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

        } catch (Exception e) {

        }

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
        ch.qos.logback.classic.Logger nodeLogger = loggerContext.getLogger(NODE_LOGGER);
        consoleAppender.ifPresent(nodeLogger::addAppender);
        nodeLogger.addAppender(rfAppender);
        nodeLogger.setAdditive(false);
        nodeLogger.setLevel(Level.TRACE);

        ch.qos.logback.classic.Logger frameworkLogger = loggerContext.getLogger("com.havving.framework");
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

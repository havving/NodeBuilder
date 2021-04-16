package com.havving.framework.config;

import lombok.Getter;
import lombok.Setter;

/**
 * NodeBuilder Script 설정
 *
 * APP_NAME("node.app.name") → APP Name 설정
 * APP_TYPE("node.app.type") → 기동 모드 설정 (FIRE/DAEMON)
 * SCAN_PKG("node.scan") → @Component 스캔 패키지명
 * LOG_LEVEL("node.log.level") → NodeBuilder 전용 Log Level 설정 (default: DEBUG)
 * STAT_METHOD("node.log.stat_method") → @Component의 methodTracing을 true로 설정한 컴포넌트의 각 메서드 수행 시간 로그 출력 여부 설정 (true/false)
 * PID_PATH("node.path.pid") → 기동 시 pid 파일을 생성할 위치 지정
 * CONFIG_PATH("node.path.conf") → NodeBuilder 설정 파일의 위치를 절대경로로 지정 (확장자 포함)
 * GC_LOOKUP("node.gc.lookup") → JVM GC 로그 출력 여부 설정 (true/false)
 * STAT_VM("node.stat.vm");
 *
 * @author HAVVING
 * @since 2021-04-16
 */
public enum InitArguments {
    APP_NAME("node.app.name"),
    APP_TYPE("node.app.type"),
    SCAN_PKG("node.scan"),
    LOG_LEVEL("node.log.level"),
    STAT_METHOD("node.log.stat_method"),
    PID_PATH("node.path.pid"),
    CONFIG_PATH("node.path.conf"),
    GC_LOOKUP("node.gc.lookup"),
    STAT_VM("node.stat.vm");
    @Getter
    private String key;
    @Getter
    @Setter
    private String value;

    InitArguments(String key) {
        this.key = key;
    }
}

package com.havving.framework.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HAVVING
 * @since 2021-04-16
 */
@Data
public final class JvmGcData implements Serializable {
    private final long id;
    private final String name;
    private final String gcType;
    private final String cause;
    private final long duration;
    private final long startTime;
    private final long endTime;
    private final Map<String, GcMemoryData> memData;

    public JvmGcData(long id, String name, String gcType, String cause, long duration, long startTime, long endTime) {
        this.id = id;
        this.name = name;
        this.gcType = gcType;
        this.cause = cause;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        memData = new HashMap<>();
    }

    @Data
    @AllArgsConstructor
    public static final class GcMemoryData implements Serializable {
        private final long memInit;
        private final long memCommitted;
        private final long memMax;
        private final long memUsed;
        private final double beforePercent;
        private final double percent;
    }
}

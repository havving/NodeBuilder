package com.havving.framework.config;

/**
 * @author HAVVING
 * @since 2021-04-18
 */
public enum AppType {
    FIRE, DAEMON;

    public static AppType getType(String type) {
        return type.equalsIgnoreCase(FIRE.toString()) ? FIRE : (type.equalsIgnoreCase(DAEMON.toString()) ? DAEMON : null);
    }
}

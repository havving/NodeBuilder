package com.havving.framework.components;

/**
 * Component 객체 정의 시 사용되는 Life-Cycle 정책
 *
 * @author HAVVING
 * @since 2021-04-19
 */
public enum BindPolicy {
    Singleton, Instant;

    public static BindPolicy is(boolean singleton) {
        return singleton ? Singleton : Instant;
    }
}

package com.havving.framework.exception;

/**
 * @author HAVVING
 * @since 2021-04-19
 */
public class ContainerInitializeException extends Exception {

    private static final String msg = "Container can't initialize.";

    public ContainerInitializeException(String message) {
        super(msg + message);
    }

    public ContainerInitializeException() {
        super(msg);
    }

    public ContainerInitializeException(Throwable e) {
        super(msg, e);
    }

    public ContainerInitializeException(String message, Throwable e) {
        super(msg + message, e);
    }
}

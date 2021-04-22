package com.havving.framework.exception;

/**
 * Component Object 중복 생성 시 발생
 *
 * @author HAVVING
 * @since 2021-04-22
 */
public class DuplicateObjectException extends RuntimeException {
    private static final String msg = "Object already exist. ";

    public DuplicateObjectException() {
        super(msg);
    }
}

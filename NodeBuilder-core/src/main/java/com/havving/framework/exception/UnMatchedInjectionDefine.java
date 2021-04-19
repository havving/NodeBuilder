package com.havving.framework.exception;

/**
 * D/I 수행 시, Injection 정의가 불일치할 경우 발생
 *
 * @author HAVVING
 * @since 2021-04-19
 */
public class UnMatchedInjectionDefine extends RuntimeException {
    private static final String msg = "Object Injection define is not valid.";

    public UnMatchedInjectionDefine(String message) {
        super(msg + message);
    }
}

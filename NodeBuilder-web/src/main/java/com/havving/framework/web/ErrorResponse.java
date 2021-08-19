package com.havving.framework.web;

import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * 에러 출력 객체
 *
 * @author HAVVING
 * @since 2021-04-29
 */
@Data
public class ErrorResponse {
    private String className;
    private String message;
    private String stackTrace;

    public ErrorResponse(Throwable e) {
        this.className = e.getClass().getName();
        this.message = e.getMessage();
        this.stackTrace = ExceptionUtils.getStackTrace(e);
    }
}

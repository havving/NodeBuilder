package com.havving.framework.annotation;

import com.havving.framework.web.HttpProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.havving.framework.web.HttpProvider.ContentType.JSON;
import static com.havving.framework.web.HttpProvider.HttpMethod.GET;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author HAVVING
 * @since 2021-04-28
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface RestBinder {
    HttpProvider.ContentType contentType() default JSON;

    HttpProvider.HttpMethod method() default GET;

    String url();
}

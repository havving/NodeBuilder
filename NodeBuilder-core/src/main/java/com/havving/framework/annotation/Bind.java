package com.havving.framework.annotation;

import javax.annotation.Resource;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Object D/I 관련 정의 인터페이스
 *
 * @author HAVVING
 * @since 2021-04-20
 */
@Target(FIELD)
@Retention(RUNTIME)
@Resource
public @interface Bind {
    /**
     * Bind 될 Component명
     * 기본일 경우, 해당 필드명을 기준으로 주입함
     *
     * @return
     */
    String name() default "";
}

package com.havving.framework.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Container 클래스에 등록될 클래스 삽입
 * name에 정의된 이름을 Key로 하여 ComponentContainer 내에 등록되어 관리된다.
 *
 * @author HAVVING
 * @since 2021-04-19
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Component {
    /**
     * 설정 시, 해당 Object의 메서드가 수행될 때 수행 시간, 입력값, 리턴 타입을 체크하여 로깅
     */
    boolean methodTracing() default false;

    String name() default "";

    String[] other() default {};

    boolean singleton() default true;
}

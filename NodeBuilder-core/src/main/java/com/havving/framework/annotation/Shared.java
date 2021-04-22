package com.havving.framework.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author HAVVING
 * @since 2021-04-22
 */
@Target({FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Inherited
public @interface Shared {
    String id() default "";

    boolean lookup() default false;

    int maxLength() default Integer.MAX_VALUE;

    Type type() default Type.Include;

    enum Type {
        Include, Separate
    }
}

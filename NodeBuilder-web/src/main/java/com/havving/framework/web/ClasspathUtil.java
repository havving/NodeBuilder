package com.havving.framework.web;

import java.io.InputStream;

/**
 * @author HAVVING
 * @since 2021-04-29
 */
public class ClasspathUtil {

    public static InputStream getResourceAsStream(final String classPath) {
        return ClasspathUtil.class.getClassLoader().getResourceAsStream(classPath);
    }
}

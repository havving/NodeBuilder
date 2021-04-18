package com.havving.framework.config;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;

/**
 * @author HAVVING
 * @since 2021-04-18
 */
@Slf4j
public class ClassPathResourceResolver extends ResourceResolver {
    private static final String CLASSPATH = "classpath:";

    @Override
    public NodeConfig read(String classPathLoader) throws Exception {
        if (!classPathLoader.toLowerCase().startsWith(CLASSPATH)) {
            log.error("This path is invalid.");
            return null;
        }

        URL url = ClassPathResourceResolver.class.getClassLoader().getResource(classPathLoader.replace(CLASSPATH, ""));
        if (url == null) throw new Exception(classPathLoader + "File couldn't find.");

        return super.readToObject(new File(url.getFile()));
    }
}

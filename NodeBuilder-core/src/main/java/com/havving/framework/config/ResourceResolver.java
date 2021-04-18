package com.havving.framework.config;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;

/**
 * @author HAVVING
 * @since 2021-04-18
 */


public abstract class ResourceResolver {
    public abstract NodeConfig read(String path) throws Exception;

    protected NodeConfig readToObject(File file) throws Exception {
        Serializer serializer = new Persister(EnumXMLMatcher.get());

        return serializer.read(NodeConfig.class, file);
    }
}

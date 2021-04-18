package com.havving.framework.config;

import java.io.File;

/**
 * @author HAVVING
 * @since 2021-04-18
 */
public class AbsolutePathResourceResolver extends ResourceResolver {

    @Override
    public NodeConfig read(String path) throws Exception {
        File configFile = new File(path);

        return super.readToObject(configFile);
    }
}

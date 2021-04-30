package com.havving.framework.web;

import com.havving.framework.Extension;
import com.havving.framework.NodeBuilder;
import com.havving.framework.exception.ContainerInitializeException;

/**
 * Web 모듈 활성화 객체
 *
 * @author HAVVING
 * @since 2021-04-30
 */

public class NodeWebAppExtension implements Extension<RestContainer> {
    @Override
    public RestContainer activate() throws ContainerInitializeException {
        RestContainer container = new RestContainer();
        container.initializeToScan(NodeBuilder.getContext().getConfig().getNodeConfig().getScanPackage());

        return container;
    }

    @Override
    public boolean initializable() {
        return NodeBuilder.getContext().getConfig().getNodeConfig().getWebApp() != null;
    }
}

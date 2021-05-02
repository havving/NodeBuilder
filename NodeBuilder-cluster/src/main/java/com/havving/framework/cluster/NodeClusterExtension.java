package com.havving.framework.cluster;

import com.havving.framework.Extension;
import com.havving.framework.NodeBuilder;
import com.havving.framework.config.NodeConfig;
import com.havving.framework.exception.ContainerInitializeException;

/**
 * @author HAVVING
 * @since 2021-05-02
 */
public class NodeClusterExtension implements Extension<ClusterContainer> {

    @Override
    public ClusterContainer activate() throws ContainerInitializeException {
        NodeConfig nodeConfig = NodeBuilder.getContext().getConfig().getNodeConfig();
        ClusterContainer container = new ClusterContainer();
        container.initializeToScan(nodeConfig.getScanPackage());

        return container;
    }

    @Override
    public boolean initializable() {
        return NodeBuilder.getContext().getConfig().getNodeConfig().getCluster() != null;
    }
}

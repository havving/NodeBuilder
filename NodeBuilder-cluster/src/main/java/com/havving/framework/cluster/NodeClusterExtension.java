package com.havving.framework.cluster;

import com.havving.framework.Extension;
import com.havving.framework.NodeBuilder;
import com.havving.framework.config.NodeConfig;

/**
 * Core 모듈에 Cluster 객체 등록
 *
 * @author HAVVING
 * @since 2021-05-02
 */
public class NodeClusterExtension implements Extension<ClusterContainer> {

    @Override
    public ClusterContainer activate() {
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

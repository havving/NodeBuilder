package com.havving.framework.cluster;

import com.havving.framework.components.Container;
import com.havving.framework.exception.ContainerInitializeException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Externalizable;

/**
 * @author HAVVING
 * @since 2021-04-30
 */
@Slf4j
public class ClusterContainer implements Container<IMap<String, SharedMap>> {
    private static final String ROOT = "nod-info";
    private IMap<String, SharedMap> clusterMap;
    private MultiMap<String, Externalizable> clusterMultiMap;
    @Getter
    private SharedMap data;
    private HazelcastInstance instance;
    private boolean ready = false;

    @Override
    public CoreExtensions getExtensionType() {
        return CoreExtensions.CLUSTER;
    }

    @Override
    public IMap<String, SharedMap> getFactory() {
        return clusterMap;
    }

    @Override
    public Container initializeToScan(String scanPackage) throws ContainerInitializeException {
        return null;
    }

    @Override
    public boolean valid() {
        return ready;
    }
}

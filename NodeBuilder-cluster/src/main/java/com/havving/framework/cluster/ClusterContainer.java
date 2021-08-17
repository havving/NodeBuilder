package com.havving.framework.cluster;

import com.havving.framework.NodeBuilder;
import com.havving.framework.NodeContext;
import com.havving.framework.annotation.Shared;
import com.havving.framework.components.ComponentPolicyFactory;
import com.havving.framework.components.Container;
import com.havving.framework.components.SingletonProxyFactory;
import com.havving.framework.config.NodeConfigListener;
import com.havving.framework.config.extensions.Cluster;
import com.havving.framework.domain.Configuration;
import com.hazelcast.config.*;
import com.hazelcast.core.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Externalizable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

/**
 * Cluster 기능 수행 class
 *
 * @author HAVVING
 * @since 2021-04-30
 */
@Slf4j
public class ClusterContainer implements Container<IMap<String, SharedMap>> {
    private static final String ROOT = "node-info";
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
    public Container initializeToScan(String scanPackage) {
        System.setProperty("hazelcast.logging.type", "slf4j");
        NodeContext context = NodeBuilder.getContext();

        Configuration globalConf = context.getConfig();
        Cluster clusterConf = globalConf.getNodeConfig().getCluster();
        log.info("Cluster configuration : {}", clusterConf);
        ComponentPolicyFactory policyFactory = context.getPolicyFactory();

        this.instance = _createInstance(clusterConf);
        this.data = _initSharedMap(globalConf, policyFactory);

        clusterMap = this.instance.getMap(ROOT);
        clusterMultiMap = this.instance.getMultiMap(ROOT);

        SingletonProxyFactory singletonProxyFactory = context.getObjectFactory();
        singletonProxyFactory.forEach((key, value) ->
                Stream.of(value.getClass().getSuperclass().getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(Shared.class))
                        .forEach(f -> {
                            Object superObj = value.getClass().getSuperclass().cast(value);
                            f.setAccessible(true);
                            Shared anno = f.getDeclaredAnnotation(Shared.class);
                            Object fieldData = null;
                            try {
                                fieldData = f.get(superObj);
                            } catch (IllegalAccessException e) {
                                log.error(e.toString(), e);
                            }

                            if (!Externalizable.class.isInstance(fieldData)) {
                                log.error("Shared data must be implement externalize.");
                            } else {
                                String id = anno.id();
                                if (anno.type().equals(Shared.Type.Include)) {
                                    this.data.addToShare(id, key, (Externalizable) fieldData);
                                } else {
                                    this.clusterMultiMap.put(id, (Externalizable) fieldData);
                                }
                            }
                            f.setAccessible(false);
                        })
        );

        clusterMap.addLocalEntryListener(new SharedMapDataListener<SharedMap>() {
            @Override
            public void entryAdded(EntryEvent<String, SharedMap> event) {
                log.debug("{} has been added.", event.getKey());
                log.trace("value - {}", event.getValue());
            }

            @Override
            public void entryEvicted(EntryEvent<String, SharedMap> event) {
                log.debug("{} has been evicted.", event.getKey());
                log.trace("value - {}", event.getValue());
            }

            @Override
            public void entryRemoved(EntryEvent<String, SharedMap> event) {
                log.debug("{} has been removed.", event.getKey());
                log.trace("value - {}", event.getValue());
            }

            @Override
            public void entryUpdated(EntryEvent<String, SharedMap> event) {
                log.info("{} has been updated.", ROOT);
                if (log.isTraceEnabled()) {
                    log.trace("{}", event.getKey());
                    log.trace("was - {}", event.getOldValue());
                    log.trace("be - {}", event.getValue());
                } else {
                    log.debug("{} - {}", event.getKey(), event.getValue());
                }
            }

            @Override
            public void mapCleared(MapEvent event) {}

            @Override
            public void mapEvicted(MapEvent event) {}
        });

        _shareToCluster(globalConf.name(), this.data);

        NodeConfigListener listener = (nodeConfig) -> {
            this.data.setConf(nodeConfig);
            log.info("Clustered NodeConfig update to cluster. {}", nodeConfig);
            _shareToCluster(globalConf.name(), this.data);
        };

        globalConf.addNodeConfigListener(listener);
        log.info("NodeConfig cluster lookup enabled.");
        this.ready = true;

        return this;
    }


    private SharedMap _shareToCluster(String nodeId, SharedMap data) {
        IMap<String, SharedMap> map = this.instance.getMap(ROOT);   // nodeId map create

        return map.put(nodeId, data);
    }


    /**
     * Creates a new Hazelcast Instance (a new node in a cluster)
     * @param - cluster configuration
     * @return - the new Hazelcast Instance
     */
    private HazelcastInstance _createInstance(Cluster clusterConf) {
        Config config = new Config();
        NetworkConfig network = config.getNetworkConfig();
        network.setPort(clusterConf.getPort());
        network.setPortCount(100);
        network.setPortAutoIncrement(false);

        JoinConfig join = network.getJoin();
        MulticastConfig multicastConfig = join.getMulticastConfig();

        if (!clusterConf.isMulticast()) {
            multicastConfig.setEnabled(false);
            TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
            clusterConf.getMembers().forEach(tcpIpConfig::addMember);
            tcpIpConfig.setEnabled(true);
        }

        if (clusterConf.getId() != null && clusterConf.getPw() != null) {
            config.getGroupConfig().setName(clusterConf.getId()).setPassword(clusterConf.getPw());
        }

        return Hazelcast.newHazelcastInstance(config);
    }


    private SharedMap _initSharedMap(Configuration globalConf, ComponentPolicyFactory policyFactory) {
        SharedMap data = new SharedMap();
        try {
            data.setAddress(InetAddress.getLocalHost().toString());
        } catch (UnknownHostException e) {
            log.error(e.toString(), e);
        }

        data.setArgs(globalConf.getVmArgs());
        data.setData(policyFactory);
        data.setConf(globalConf.getNodeConfig());

        return data;
    }

    @Override
    public boolean valid() {
        return ready;
    }
}

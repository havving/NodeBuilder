package com.havving.framework.domain;

import com.google.common.collect.Sets;
import com.havving.framework.config.JvmArguments;
import com.havving.framework.config.NodeConfig;
import com.havving.framework.config.NodeConfigListener;
import com.havving.framework.exception.DuplicateObjectException;
import lombok.Getter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 글로벌 설정 관련 클래스
 * Externalizable로 생성되어, Cluster 내에서의 공통 설정에 대응함
 *
 * @author HAVVING
 * @since 2021-04-22
 */
public class Configuration implements Externalizable {
    private static final long serialVersionUID = 6688944386567642559L;

    private static Configuration conf;
    private String APPLICATION_NAME;
    @Getter
    private JvmArguments vmArgs;    // 기동 시, JVM에 등록된 설정 정보값
    @Getter
    private NodeConfig nodeConfig;  // 설정 정보 객체
    private Set<NodeConfigListener> nodeConfigListeners = Collections.emptySet();


    /**
     * static init 메서드를 이용하여 생성됨
     * @param appName
     */
    private Configuration(String appName) {
        this.APPLICATION_NAME = appName;

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMXBean.getInputArguments();
        vmArgs = JvmArguments.create(arguments);
    }


    /**
     * Configuration 클래스를 이용하기 위해 실행될 메서드
     * Application의 이름 설정 및 각 모듈들의 설정을 담기 위한 EnumMap을 초기화 함
     *
     * @param appName
     * @return
     */
    public static Configuration init(String appName) {
        synchronized (Configuration.class) {
            if (conf != null) {
                throw new DuplicateObjectException();
            }
            return new Configuration(appName);
        }
    }


    /**
     * @param config 외부에서 생성된 설정 정보를 내부에 저장
     */
    public void apply(NodeConfig config) {
        nodeConfig = config;
        if (!nodeConfigListeners.isEmpty()) {
            nodeConfigListeners.forEach(e -> e.update(config));
        }
    }


    public String name() {
        return APPLICATION_NAME;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(APPLICATION_NAME);
        out.writeObject(vmArgs);
        out.writeObject(nodeConfig);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.APPLICATION_NAME = (String) in.readObject();
        this.vmArgs = (JvmArguments) in.readObject();
        this.nodeConfig = (NodeConfig) in.readObject();
    }


    /**
     * NodeConfig에 대한 listener를 등록하여 config update시 사용함
     *
     * @param nodeConfigListener NodeConfigListener를 상속받은 클래스
     * @return NodeConfigListener의 전체 사이즈
     */
    public int addNodeConfigListener(NodeConfigListener nodeConfigListener) {
        if (nodeConfigListeners.isEmpty()) {
            nodeConfigListeners = Sets.newHashSet();
        }
        this.nodeConfigListeners.add(nodeConfigListener);

        return this.nodeConfigListeners.size();
    }
}

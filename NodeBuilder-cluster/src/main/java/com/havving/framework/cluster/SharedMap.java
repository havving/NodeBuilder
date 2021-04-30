package com.havving.framework.cluster;

import com.havving.framework.components.ComponentPolicyFactory;
import com.havving.framework.config.JvmArguments;
import com.havving.framework.config.NodeConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * Cluster로 공유할 데이터의 도메인 객체
 *
 * @author HAVVING
 * @since 2021-04-30
 */
@ToString
public class SharedMap implements Externalizable {
    private static final long serialVersionUID = 3045007748220503320L;

    @Getter @Setter
    private String address;
    @Getter @Setter
    private JvmArguments args;
    @Getter @Setter
    private NodeConfig conf;
    @Getter @Setter
    private ComponentPolicyFactory data;
    @Getter
    private Map<String, ShareFieldPolicy> shared;

    public SharedMap() {
        this.shared = new HashMap<>();
    }

    public SharedMap addToShare(String id, String componentId, Externalizable fieldData) {
        this.shared.put(id, new ShareFieldPolicy(componentId, fieldData));

        return this;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(address);
        out.writeObject(args);
        out.writeObject(conf);
        out.writeObject(data);
        out.writeObject(shared);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.address = (String) in.readObject();
        this.args = (JvmArguments) in.readObject();
        this.conf = (NodeConfig) in.readObject();
        this.data = (ComponentPolicyFactory) in.readObject();
        this.shared = (Map<String, ShareFieldPolicy>) in.readObject();
    }
}

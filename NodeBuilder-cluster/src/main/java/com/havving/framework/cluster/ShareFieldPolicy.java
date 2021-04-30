package com.havving.framework.cluster;

import lombok.Data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 데이터 공유 정책을 정의한 객체
 *
 * @author HAVVING
 * @since 2021-04-30
 */
@Data
public class ShareFieldPolicy implements Externalizable {
    private static final long serialVersionUID = 5202163692152189582L;

    private String componentId;
    private String componentType;
    private Externalizable objectData;
    private transient Class<?> type;

    public ShareFieldPolicy(String componentId, Externalizable objectData) {
        this.componentId = componentId;
        this.componentType = type.getName();
        this.objectData = objectData;
        this.type = objectData.getClass();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(componentId);
        out.writeObject(componentType);
        out.writeObject(objectData);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.componentId = (String) in.readObject();
        this.componentType = (String) in.readObject();
        this.objectData = (Externalizable) in.readObject();
        if (this.componentType != null)
            this.type = Class.forName(this.componentType);
    }
}

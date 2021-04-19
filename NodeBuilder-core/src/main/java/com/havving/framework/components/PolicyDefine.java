package com.havving.framework.components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Locale;

/**
 * Component 객체의 Life-Cycle 정책에 대한 정의 담당
 *
 * @author HAVVING
 * @since 2021-04-19
 */
@ToString
@EqualsAndHashCode
public class PolicyDefine implements Externalizable {
    private static final long serialVersionUID = -8165353007431377819L;

    @Getter
    private String objectName;
    private String className;
    @Getter
    private transient Class<?> clz;

    public PolicyDefine(final String objectName, final Class<?> clz) {
        if (objectName.trim().length() < 1) {
            String clzName = clz.getSimpleName();
            String pre = clzName.substring(0, 1).toLowerCase();
            this.objectName = pre + clzName.substring(0, 1);
        } else {
            this.objectName = objectName;
        }
        this.clz = clz;
        this.className = clz.getName();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(objectName);
        out.writeObject(className);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.objectName = (String) in.readObject();
        this.className = (String) in.readObject();
        if (this.className != null)
            this.clz = Class.forName(this.className);
    }
}

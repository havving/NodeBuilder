package com.havving.framework.config.extensions;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import static com.havving.framework.config.extensions.Scheduler.Type.Java;

/**
 * @author HAVVING
 * @since 2021-04-18
 */
@Data
@Element
public class Scheduler implements Externalizable {
    private static final long serialVersionUID = -9210286992342759236L;

    @Attribute
    private String name;
    @Attribute(required = false)
    private Type type = Java;
    @Attribute
    private int threads;
    @Attribute(required = false)
    private boolean isClustered = false;
    @Element(name = "db", required = false)
    private SchedulerDB dbConfig;


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeObject(type);
        out.writeObject(threads);
        out.writeObject(isClustered);
        out.writeObject(dbConfig);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.name = (String) in.readObject();
        this.type = (Type) in.readObject();
        this.threads = (int) in.readObject();
        this.isClustered = (boolean) in.readObject();
        this.dbConfig = (SchedulerDB) in.readObject();
    }

    public enum Type {
        Quartz, Java
    }
}

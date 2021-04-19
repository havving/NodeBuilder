package com.havving.framework.config.extensions;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

/**
 * @author HAVVING
 * @since 2021-04-18
 */
@Data
@Element
public class Cluster implements Externalizable {
    private static final long serialVersionUID = 8028614446242024077L;

    @Attribute
    private int port;
    @Attribute
    private String id;
    @Attribute
    private String pw;
    @Attribute
    private boolean multicast;
    @ElementList(name = "members")
    private Collection<String> members;


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(port);
        out.writeObject(id);
        out.writeObject(pw);
        out.writeObject(multicast);
        out.writeObject(members);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.port = (int) in.readObject();
        this.id = (String) in.readObject();
        this.pw = (String) in.readObject();
        this.multicast = (boolean) in.readObject();
        this.members = (Collection<String>) in.readObject();
    }
}

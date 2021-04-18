package com.havving.framework.config.extentions;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author HAVVING
 * @since 2021-04-18
 */
@Data
@Element
public class WebApp implements Externalizable {
    private static final long serialVersionUID = 5564227712632586443L;

    @Attribute
    private int port;
    @Attribute(required = false)
    private String context = "";
    @Attribute(name = "max-connection", required = false)
    private int maxConnection = 50;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(port);
        out.writeObject(context);
        out.writeObject(maxConnection);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.port = (int) in.readObject();
        this.context = (String) in.readObject();
        this.maxConnection = (int) in.readObject();
    }
}

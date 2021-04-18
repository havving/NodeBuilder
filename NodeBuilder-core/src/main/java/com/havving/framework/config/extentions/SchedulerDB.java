package com.havving.framework.config.extentions;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

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
public class SchedulerDB implements Externalizable {
    private static final long serialVersionUID = 6480154837356545537L;

    @Attribute(name = "name")
    private String name;
    @Attribute(name = "driverClass")
    private String driverClass;
    @Attribute(name = "delegateClass")
    private String delegateClass;
    @Attribute(name = "url")
    private String url;
    @Attribute(name = "user")
    private String user;
    @Attribute(name = "pw")
    private String pw;
    @Attribute(name = "max-connection")
    private int maxConn;
    @Attribute(name = "valid-query", required = false)
    private String validationQuery = "select 1 from dual;";
    @Attribute(name = "prefix", required = false)
    private String tablePrefix = "SCHD_";

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeObject(driverClass);
        out.writeObject(delegateClass);
        out.writeObject(url);
        out.writeObject(user);
        out.writeObject(pw);
        out.writeObject(maxConn);
        out.writeObject(validationQuery);
        out.writeObject(tablePrefix);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.name = (String) in.readObject();
        this.driverClass = (String) in.readObject();
        this.delegateClass = (String) in.readObject();
        this.url = (String) in.readObject();
        this.user = (String) in.readObject();
        this.pw = (String) in.readObject();
        this.maxConn = (int) in.readObject();
        this.validationQuery = (String) in.readObject();
        this.tablePrefix = (String) in.readObject();
    }
}

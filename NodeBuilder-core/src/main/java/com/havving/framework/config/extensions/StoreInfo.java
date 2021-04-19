package com.havving.framework.config.extensions;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;

/**
 * @author HAVVING
 * @since 2021-04-18
 */
@Data
@Element
public class StoreInfo implements Externalizable {
    private static final long serialVersionUID = 420889623678264006L;

    @Attribute(name = "name")
    private String storeName;
    @Attribute(name = "path")
    private String storePath;
    @Attribute(name = "max-amount", required = false)
    private int maxStoreAmounts = Integer.MAX_VALUE;
    @ElementList(name = "searchables", required = false)
    private List<String> searchableFields = Collections.emptyList();

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(storeName);
        out.writeObject(storePath);
        out.writeObject(maxStoreAmounts);
        out.writeObject(searchableFields);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.storeName = (String) in.readObject();
        this.storePath = (String) in.readObject();
        this.maxStoreAmounts = (int) in.readObject();
        this.searchableFields = (List<String>) in.readObject();
    }
}

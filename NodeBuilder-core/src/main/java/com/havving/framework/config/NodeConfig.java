package com.havving.framework.config;

import com.havving.framework.config.extentions.Cluster;
import com.havving.framework.config.extentions.Scheduler;
import com.havving.framework.config.extentions.StoreInfo;
import com.havving.framework.config.extentions.WebApp;
import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author HAVVING
 * @since 2021-04-18
 */
@Data
@Root(name = "node", strict = false)
public class NodeConfig implements Serializable {
    private static final long serialVersionUID = -5044744512834052454L;

    @Attribute(name = "scan-package")
    private String scanPackage;
    @Attribute(name = "app-type", required = false)
    private AppType appType = AppType.DAEMON;
    @Element(required = false)
    private Cluster cluster;
    @Element(name = "scheduler", required = false)
    private Scheduler scheduler;
    @ElementList(name = "stores", required = false)
    private Collection<StoreInfo> stores;
    @Element(name = "webapp", required = false)
    private WebApp webApp;
}

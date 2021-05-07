import com.havving.framework.NodeBuilder;
import com.havving.framework.cluster.SharedMap;
import com.havving.framework.components.Container;
import com.havving.framework.config.AppType;
import com.havving.framework.config.NodeConfig;
import com.havving.framework.domain.Configuration;
import com.hazelcast.core.IMap;
import org.junit.Test;

import java.util.Map;

/**
 * @author HAVVING
 * @since 2021-05-06
 */
public class ClusterTest {

    @Test
    public void test() {
        System.setProperty("hazelcast.logging.type", "slf4j");
        System.setProperty("node.app.name", "TEST01");
        System.setProperty("node.path.conf", "D:\\Project\\NodeBuilder\\Tutorial\\src\\main\\resources\\node-conf.xml");
        NodeBuilder.main(null);

        Container clusterContainer = NodeBuilder.getContext().getContainer(Container.CoreExtensions.CLUSTER);
        IMap<String, SharedMap> factory = (IMap<String, SharedMap>) clusterContainer.getFactory();
        for (Map.Entry<String, SharedMap> f : factory.entrySet()) {
            System.out.println(f.getKey() + ":" + f.getValue());
        }
    }

    @Test
    public void updateTest() {
        System.setProperty("node.path.conf", "D:\\Project\\NodeBuilder\\Tutorial\\src\\main\\resources\\node-conf.xml");
        NodeBuilder.main(null);

        Container clusterContainer = NodeBuilder.getContext().getContainer(Container.CoreExtensions.CLUSTER);
        Map<String, SharedMap> fac = (IMap<String, SharedMap>) clusterContainer.getFactory();
        for (Map.Entry<String, SharedMap> f : fac.entrySet()) {
            System.out.println(f.getKey() + ":" + f.getValue());
        }

        Configuration configuration = NodeBuilder.getContext().getConfig();
        NodeConfig nodeConfig = configuration.getNodeConfig();
        nodeConfig.setAppType(AppType.FIRE);
        configuration.apply(nodeConfig);
    }

}

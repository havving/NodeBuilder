import com.google.common.reflect.TypeToken;
import com.havving.framework.NodeBuilder;
import com.havving.framework.cluster.SharedMap;
import com.havving.framework.components.Container;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author HAVVING
 * @since 2021-05-07
 */
public class RestTest {

    @Test
    public void test() throws Exception {
        System.setProperty("node.app.name", "TEST01");
        System.setProperty("node.path.conf", "D:\\Project\\NodeBuilder\\Tutorial\\src\\main\\resources\\node-conf.xml");
        NodeBuilder.main(null);

        Container webContainer = NodeBuilder.getContext().getContainer(Container.CoreExtensions.WEB);
        Map<String, SharedMap> fac = webContainer.getFactory();
        for (Map.Entry<String, SharedMap> f : fac.entrySet()) {
            System.out.println(f.getKey() + ":" + f.getValue());
        }

        while (true) {
            Thread.sleep(5000);
        }
    }

}

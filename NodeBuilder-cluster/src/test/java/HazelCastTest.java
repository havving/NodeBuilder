import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

/**
 * @author HAVVING
 * @since 2021-05-07
 */
public class HazelCastTest {

    private static HazelcastInstance newInstance(String name, int port) {
        Config config = new Config();
        config.setInstanceName(name);
        config.getGroupConfig().setName("testC").setPassword("testC");

        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(port).setPortAutoIncrement(true);

        JoinConfig join = networkConfig.getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().addMember("192.168.10.242").addMember("192.168.10.122")
                .setRequiredMember("192.168.10.122").setEnabled(true);
        networkConfig.getInterfaces().setEnabled(true).addInterface("192.168.10.*");

        return Hazelcast.newHazelcastInstance(config);
    }


    @Test
    @Ignore
    public void config() throws InterruptedException {
        HazelcastInstance insA = newInstance("app01", 9999);
        Map<String, String> mapA = insA.getMap("testA");
        mapA.put("A", "A");
        mapA.put("B", "B");
        mapA.put("C", "C");
        mapA.put("D", "D");

        Map<String, String> mapB = insA.getMap("testB");
        mapB.put("A1", "A1");
        mapB.put("B1", "B1");
        mapB.put("C1", "C1");
        mapB.put("D1", "D1");

        Map<String, String> mapC = insA.getMap("testC");
        mapC.put("A2", "A2");
        mapC.put("B2", "B2");
        mapC.put("C2", "C2");
        mapC.put("D2", "D2");

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("192.168.10.122:9999")
                .setSmartRouting(true).setRedoOperation(true)
                .setConnectionTimeout(5000).setConnectionAttemptLimit(5).setConnectionAttemptPeriod(5000);
        clientConfig.setGroupConfig(new GroupConfig("testC", "testC"));

        Thread.sleep(1000);

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        IMap<String, String> map = client.getMap("testA");
        System.out.println(map.size());

        for (Map.Entry<String, String> e : map.entrySet()) {
            System.out.println(e.getKey() + ":" + e.getValue());
        }

        for (Map.Entry<String, String> e : mapA.entrySet()) {
            System.out.println(e.getKey() + ":" + e.getValue());
        }
    }

}

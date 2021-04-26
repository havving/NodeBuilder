package com.havving.framework;

import com.havving.framework.NodeBuilder;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author HAVVING
 * @since 2021-04-26
 */
@Ignore
public class NodeBuilderTest {

    @Test
    public void mainMockTest1() {
        System.setProperty("node.app.name", "TEST1");
        System.setProperty("node.scan", "com.havving.example");
        NodeBuilder.main(null);

    }
}

package com.havving.framework.config;

import com.havving.framework.config.JvmArguments;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import static java.lang.System.out;

/**
 * @author HAVVING
 * @since 2021-04-26
 */
public class JvmArgumentsTest {

    @Test
    public void createTest() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMXBean.getInputArguments();
        JvmArguments.create(arguments).getArgsMap().forEach((k, v) -> out.println(k + " = " + v));
    }

    @Test
    @Ignore
    public void vmArgsTest() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMXBean.getInputArguments();

        arguments.forEach(out::println);
    }
}

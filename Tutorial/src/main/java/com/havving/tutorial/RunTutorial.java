package com.havving.tutorial;

import com.google.gson.GsonBuilder;
import com.havving.framework.NodeBuilder;

import static com.havving.framework.config.InitArguments.*;

/**
 * @author HAVVING
 * @since 2021-04-27
 */
public class RunTutorial {

    public static void main(String[] args) throws Exception {
        System.setProperty(APP_NAME.getKey(), "TEST1");
        System.setProperty(CONFIG_PATH.getKey(), "D:\\Project\\NodeBuilder\\Tutorial\\src\\main\\resources\\node-conf.xml");
        System.setProperty(APP_TYPE.getKey(), "daemon");
        System.setProperty(GC_LOOKUP.getKey(), "");
        System.setProperty(LOG_LEVEL.getKey(), "debug");
        System.setProperty(STAT_VM.getKey(), "true");

        NodeBuilder.main(args);
        NodeBuilder.registGCHandler((jvmGcData) -> {
            System.out.println("GC PRINT---");
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(jvmGcData));

            return jvmGcData;
        });

        NodeBuilder.hold();
    }
}

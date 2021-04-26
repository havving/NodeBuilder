package com.havving.framework.config;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HAVVING
 * @since 2021-04-22
 */
@Slf4j
@ToString
public final class JvmArguments implements Externalizable {
    private static final long serialVersionUID = 660938109902477100L;
    private static transient final String EMPTY = "";

    @Getter
    private Map<String, String> argsMap;

    public JvmArguments() {
    }

    private JvmArguments(int length) {
        argsMap = new HashMap<>(length);
    }

    public static JvmArguments create(List<String> args) {
        JvmArguments arguments = new JvmArguments(args.size());
        log.info("args size={}", args.size());
        for (String arg : args) {
            log.info("{}", arg);
        }

        args.forEach(e -> {
            if (e.startsWith("-D")) e = e.substring(2);
            if (e.startsWith("-")) e = e.substring(1);

            if (e.contains("=")) {
                String[] kv = e.split("=");
                if (kv.length > 1) {
                    arguments.argsMap.put(kv[0], kv[1]);
                } else {
                    arguments.argsMap.put(kv[0], EMPTY);
                }
            } else {
                arguments.argsMap.put(e, EMPTY);
            }
        });

        return arguments;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(argsMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.argsMap = (Map<String, String>) in.readObject();
    }
}

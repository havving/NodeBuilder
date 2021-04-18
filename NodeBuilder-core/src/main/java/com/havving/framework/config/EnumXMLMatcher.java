package com.havving.framework.config;

import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

/**
 * @author HAVVING
 * @since 2021-04-18
 */
public class EnumXMLMatcher implements Transform<Enum> {
    private Class type;

    public EnumXMLMatcher(Class type) {
        this.type = type;
    }

    public static Matcher get() {
        return new Matcher() {
            @Override
            public Transform match(Class type) throws Exception {
                if (type.isEnum()) {
                    return new EnumXMLMatcher(type);
                }
                return null;
            }
        };
    }

    @Override
    public Enum read(String value) throws Exception {
        for (Object o : type.getEnumConstants()) {
            if (o.toString().equalsIgnoreCase(value)) {
                return (Enum) o;
            }
        }
        return null;
    }

    @Override
    public String write(Enum value) throws Exception {
        return value.toString();
    }
}

package com.havving.framework.components;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Component 객체의 Proxy를 저장하는 객체
 *
 * @author HAVVING
 * @since 2021-04-19
 */
public class SingletonProxyFactory extends ConcurrentHashMap<String, Object> {
    private static final long serialVersionUID = -2713385664248859896L;

    @Override
    public Object get(final Object id) {
        if (id instanceof Class)
            return this.getForClass((Class<?>) id);
        Object man = super.get(id);

        return super.get(id) == null ? null : man;
    }



    @SuppressWarnings("unchecked")
    private <T> T getForClass(final Class<?> clz) {
        T result = null;
        for (Map.Entry<String, Object> ent : entrySet()) {
            if (clz.isAssignableFrom(ent.getValue().getClass())) {
                result = (T) ent.getValue();
                break;
            }
        }
        return result;
    }


}

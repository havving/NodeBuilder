package com.havving.framework.config;

/**
 * @author HAVVING
 * @since 2021-04-22
 */
@FunctionalInterface
public interface NodeConfigListener {
    void update(NodeConfig config);
}

package com.havving.framework.web;

/**
 * @author HAVVING
 * @since 2021-04-28
 */
public class HttpProvider {
    public enum HttpMethod {
        HEAD, GET, POST, PUT, DELETE
    }

    public enum ContentType {
        JSON;

        public String getMimeType() {
            return null;
        }
    }
}

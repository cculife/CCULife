package org.zankio.ccudata.base.source.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPParameter {
    public enum Action {
        ADD,
        REMOVE,
        REPLACE
    }
    public enum HTTPMethod {
        GET,
        POST,
        PUT,
        HEAD,
        DELETE,
        CONNECT,
        OPTIONS,
        TRACE
    }

    private String url = "";
    private HTTPMethod method = HTTPMethod.GET;
    private Map<String, List<String>> queryString = new HashMap<>();
    private Map<String, List<String>> headers = new HashMap<>();
    private Map<String, List<String>> fields = new HashMap<>();
    private Map<String, List<String>> cookies = new HashMap<>();
    private boolean followRedirect;

    public HTTPMethod method() { return method; }
    public HTTPParameter method(HTTPMethod value) {
        method = value;
        return this;
    }

    public String url() { return url; }
    public HTTPParameter url(String value) {
        url = value;
        return this;
    }

    public Map<String, List<String>> queryStrings() { return queryString; }
    public HTTPParameter queryStrings(String key, Action action) {
        return queryStrings(key, null, action);
    }
    public HTTPParameter queryStrings(String key, String value) {
        return queryStrings(key, value, Action.ADD);
    }
    public HTTPParameter queryStrings(String key, String value, Action action) {
        mapAction(queryString, key, value, action);
        return this;
    }

    public Map<String, List<String>> headers() { return headers; }
    public HTTPParameter headers(String key, Action action) {
        return headers(key, null, action);
    }
    public HTTPParameter headers(String key, String value) {
        return headers(key, value, Action.ADD);
    }
    public HTTPParameter headers(String key, String value, Action action) {
        mapAction(headers, key, value, action);
        return this;
    }

    public Map<String, List<String>> fields() { return fields; }
    public HTTPParameter fields(String key, Action action) {
        return fields(key, null, action);
    }
    public HTTPParameter fields(String key, String value) {
        return fields(key, value, Action.ADD);
    }
    public HTTPParameter fields(String key, String value, Action action) {
        mapAction(fields, key, value, action);
        return this;
    }

    public Map<String, List<String>> cookies() { return cookies; }
    public HTTPParameter cookies(String key, Action action) {
        return cookies(key, null, action);
    }
    public HTTPParameter cookies(String key, String value) {
        return cookies(key, value, Action.ADD);
    }
    public HTTPParameter cookies(String key, String value, Action action) {
        mapAction(cookies, key, value, action);
        return this;
    }

    private void mapAction(Map<String, List<String>> map, String key, String value, Action action) {
        List<String> values = map.get(key);
        switch (action) {
            case ADD:
                if (values == null) {
                    values = new ArrayList<>();
                    map.put(key, values);
                }
                values.add(value);
                break;
            case REMOVE:
                map.remove(key);
                break;
            case REPLACE:
                values = new ArrayList<>();
                map.put(key, values);
                values.add(value);
                break;
        }
    }

    public boolean followRedirect() { return followRedirect; }
    public HTTPParameter followRedirect(boolean value) {
        followRedirect = value;
        return this;
    }
}

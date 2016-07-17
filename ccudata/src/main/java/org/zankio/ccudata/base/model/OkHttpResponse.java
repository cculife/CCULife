package org.zankio.ccudata.base.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.Response;

public class OkHttpResponse extends HttpResponse {
    Response response;
    Map<String, String> cookies;

    public OkHttpResponse(Response response) { this.response = response; }
    @Override
    public String string() throws IOException { return response.body().string(); }
    @Override
    public byte[] bytes() throws IOException { return response.body().bytes(); }

    @Override
    public String cookie(String name) {
        if (cookies == null) {
            cookies = new HashMap<>();

            List<Cookie> cookieList = Cookie.parseAll(null, response.headers());
            for (Cookie cookie : cookieList) {
                cookies.put(cookie.name(), cookie.value());
            }
        }

        return cookies.get(name);
    }

    @Override
    public String header(String name) { return response.header(name); }

    @Override
    public String url() {
        return response.request().url().toString();
    }

    @Override
    public List<String> headers(String name) { return response.headers(name); }
}

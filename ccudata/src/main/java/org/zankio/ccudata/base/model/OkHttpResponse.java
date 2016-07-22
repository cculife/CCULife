package org.zankio.ccudata.base.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.Response;

public class OkHttpResponse extends HttpResponse {
    String charset;
    Response response;
    Map<String, String> cookies;

    public OkHttpResponse(Response response) { this.response = response; }
    public OkHttpResponse(Response response, String charset) {
        this.response = response;
        this.charset = charset;
    }
    @Override
    public String string() throws IOException {
        if (charset != null) return new String(bytes(), charset);
        return response.body().string();
    }

    @Override
    public byte[] bytes() throws IOException { return response.body().bytes(); }

    public HttpResponse cookieJar(CookieJar cookieJar) {
        cookies = new HashMap<>();

        List<Cookie> cookieList = cookieJar.loadForRequest(response.request().url());
        for (Cookie cookie : cookieList) {
            cookies.put(cookie.name(), cookie.value());
        }
        return this;
    }

    @Override
    public String cookie(String name) {
        if (cookies == null) {
            cookies = new HashMap<>();

            List<Cookie> cookieList = Cookie.parseAll(response.request().url(), response.headers());
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

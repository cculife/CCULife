package org.zankio.ccudata.base.source.http;

import org.junit.Assert;
import org.junit.Test;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.http.annotation.QueryString;
import org.zankio.ccudata.base.source.http.annotation.Url;

public class HTTPStringSourceTest {

    private static final String OUTPUT_STR = "OUTPUT TEXT EXAMPLE!@#$%^&*(";
    //@Url("https://httpbin.org/get")
    //@Url("http://echo.jsontest.com/test/test")
    @Url("http://urlecho.appspot.com/echo")
    @QueryString({
            "status", "200",
            "Content-Type", "text/html",
            "body", OUTPUT_STR
    })
    class ExampleSource extends HTTPStringSource<Void, String> {
        @Override
        protected String parse(Request<String, Void> request, HttpResponse response, String body) throws Exception {
            return body;
        }
    }

    @Test
    public void testFetch() throws Exception {
        HTTPStringSource<?, String> source;
        String result;

        source = new ExampleSource();
        result = source.fetch(new Request<>());

        System.out.println(result);
        Assert.assertEquals(OUTPUT_STR, result);
    }
}
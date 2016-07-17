package org.zankio.ccudata.base.source.http;

import org.junit.Assert;
import org.junit.Test;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.http.annontation.QueryString;
import org.zankio.ccudata.base.source.http.annontation.Url;

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
    class ExampleSource extends HTTPStringSource { }

    @Test
    public void testFetch() throws Exception {
        HTTPStringSource source;
        String result;

        source = new ExampleSource();
        result = source.fetch(new Request());

        System.out.println(result);
        Assert.assertEquals(OUTPUT_STR, result);
    }
}
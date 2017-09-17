package org.zankio.ccudata.base.source.http;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.JSON;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.http.annotation.QueryString;
import org.zankio.ccudata.base.source.http.annotation.Url;

public class HTTPJSONSourceTest {
    private static final String KEY = "testKEY";
    private static final String VALUE = "testVALUE";

    @Url("http://httpbin.org/get")
    @QueryString({
            KEY, VALUE
    })
    class ExampleSource extends HTTPJSONSource<Void, String> {
        @Override
        protected String parse(Request<String, Void> request, HttpResponse response, JSON json) throws JSONException {
            return json.object().getJSONObject("args").getString(KEY);
        }
    }

    @Test
    public void testParse() throws Exception {
        HTTPJSONSource<Void, String> source;
        String result;

        source = new ExampleSource();
        result = source.fetch(new Request<>());

        System.out.println(result);
        Assert.assertEquals(VALUE, result);
    }
}
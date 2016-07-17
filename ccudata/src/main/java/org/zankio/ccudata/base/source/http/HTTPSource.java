package org.zankio.ccudata.base.source.http;

import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.OkHttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.FetchParseSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public abstract class HTTPSource<TData> extends FetchParseSource<TData, HttpResponse> {
    private static final String HTTP_PARAMETERS = "HTTP_PARAMETERS";

    @Override
    protected HttpResponse fetch(Request request, boolean inner) throws Exception {
        initHTTPRequest(request);
        HTTPParameter parameter = httpParameter(request);

        OkHttpClient client = makeClient(parameter);

        okhttp3.Request httpRequest = makeRequest(parameter);
        return new OkHttpResponse(client.newCall(httpRequest).execute());//.body().string();
    }

    public OkHttpClient makeClient(HTTPParameter parameter) {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .followRedirects(parameter.followRedirect())
                .followSslRedirects(parameter.followRedirect())
                .build();
    }

    private okhttp3.Request makeRequest(HTTPParameter parameter) {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        HttpUrl.Builder url = HttpUrl.parse(parameter.url()).newBuilder();
        builder.method(parameter.method().name(), makeRequestBody(parameter));

        //QueryString
        for (Map.Entry<String, List<String>> map: parameter.queryStrings().entrySet()) {
            String key = map.getKey();
            List<String> values = map.getValue();

            for(String value: values)
                url.addQueryParameter(key, value);
        }

        //Header
        for (Map.Entry<String, List<String>> map: parameter.headers().entrySet()) {
            String key = map.getKey();
            List<String> values = map.getValue();

            for(String value: values)
                builder.header(key, value);
        }

        //Cookie
        Map<String, List<String>> cookies = parameter.cookies();
        StringBuilder cookieHeader = new StringBuilder();
        for (Map.Entry<String, List<String>> map: parameter.headers().entrySet()) {
            String key = map.getKey();
            List<String> values = map.getValue();

            for(String value: values)
                cookieHeader.append(key).append("=").append(value).append(";");
        }
        builder.header("Cookie", cookieHeader.toString());

        return builder.url(url.build()).build();
    }

    private okhttp3.RequestBody makeRequestBody(HTTPParameter parameter) {
        Map<String, List<String>> fields = parameter.fields();
        FormBody.Builder formBody = new FormBody.Builder();

        for (Map.Entry<String, List<String>> map: fields.entrySet()) {
            String key = map.getKey();
            List<String> values = map.getValue();

            for(String value: values)
                formBody.add(key, value);
        }

        return formBody.build();

        //File[] files = prop.getFile(request);
        //MultipartBody.Builder multipartBody = new MultipartBody.Builder();
        //multipartBody.addFormDataPart(fields[i], fields[i + 1]);

        /*
        if (files.length > 0) {
            for (int i = 0; i < files.length; i += 2) {
                multipartBody.addFormDataPart(files[i], , files[i + 1]);
            }
        }*/

        //return null;
    }

    public void initHTTPRequest(Request request) {}

    public HTTPParameter httpParameter(Request request) {
        return request.storage().get(HTTP_PARAMETERS, HTTPParameter.class);
    }
}

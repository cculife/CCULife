package org.zankio.ccudata.base.source.http;

import org.json.JSONException;
import org.json.JSONObject;
import org.zankio.ccudata.base.model.HttpResponse;

public abstract class HTTPJSONSource<TData> extends HTTPSource<TData>{

    @Override
    protected TData parse(HttpResponse body) throws Exception {
        return parse(body, new JSONObject(body.string()));
    }

    protected abstract TData parse(HttpResponse response, JSONObject json) throws JSONException;
}

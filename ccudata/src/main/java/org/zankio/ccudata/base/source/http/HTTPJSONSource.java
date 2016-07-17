package org.zankio.ccudata.base.source.http;

import org.json.JSONException;
import org.json.JSONObject;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;

public abstract class HTTPJSONSource<TArgument, TData> extends HTTPSource<TArgument, TData>{

    @Override
    protected TData parse(Request<TData, TArgument> request, HttpResponse response) throws Exception {
        return parse(request, response, new JSONObject(response.string()));
    }

    protected abstract TData parse(Request<TData, TArgument> request, HttpResponse response, JSONObject json) throws JSONException;
}

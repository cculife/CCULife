package org.zankio.ccudata.base.source.http;

import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;

public abstract class HTTPStringSource<TArgument, TData> extends HTTPSource<TArgument, TData>{
    @Override
    protected TData parse(Request<TData, TArgument> request, HttpResponse response) throws Exception {
        return parse(request, response, response.string());
    }

    protected abstract TData parse(Request<TData, TArgument> request, HttpResponse response, String body) throws Exception;
}

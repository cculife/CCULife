package org.zankio.ccudata.base.source.http;

import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;

public abstract class HTTPStringSource<TArgument> extends HTTPSource<TArgument, String>{
    @Override
    protected String parse(Request<String, TArgument> request, HttpResponse response) throws Exception {
        return response.string();
    }
}

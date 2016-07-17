package org.zankio.ccudata.base.source.http;

import org.zankio.ccudata.base.model.HttpResponse;

public abstract class HTTPStringSource extends HTTPSource<String>{
    @Override
    protected String parse(HttpResponse body) throws Exception {
        return body.string();
    }
}

package org.zankio.ccudata.base.source;

import org.zankio.ccudata.base.model.Request;

public abstract class FetchParseSource<TData, TFetch> extends BaseSource<TData>{

    protected abstract TData parse(TFetch body) throws Exception;

    @Override
    public TData fetch(Request request) throws Exception {
        return parse(fetch(request, true));
    }

    protected abstract TFetch fetch(Request request, boolean inner) throws Exception;

    @Override
    public String getType() {
        return null;
    }
}

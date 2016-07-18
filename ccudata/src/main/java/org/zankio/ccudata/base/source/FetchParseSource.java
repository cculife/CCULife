package org.zankio.ccudata.base.source;

import org.zankio.ccudata.base.model.Request;

public abstract class FetchParseSource<TArgument, TData, TFetch> extends BaseSource<TArgument, TData>{

    protected abstract TData parse(Request<TData, TArgument> request, TFetch response) throws Exception;

    @Override
    public TData fetch(Request<TData, TArgument> request) throws Exception {
        return parse(request, fetch(request, true));
    }

    protected abstract TFetch fetch(Request<TData, TArgument> request, boolean inner) throws Exception;

    @Override
    public String getPrimaryType() {
        return null;
    }
}

package org.zankio.ccudata.base.source;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.model.Response;

import rx.Observable;

public abstract class BaseSource<TData> {
    protected Repository context;

    public SourceProperty.Level getOrder() {
        return SourceProperty.Level.LOW;
    }

    public void before(Request request) {
        Observable.just(request)
                .compose(context.preProgressRequest())
                .toBlocking()
                .single();
    }

    public abstract TData fetch(Request request) throws Exception;

    public void after(Response response) {
        Observable.just(response)
                .compose(context.postProgressResponse())
                .toBlocking()
                .single();
    }

    public abstract String getType();

    public Repository getContext() {
        return context;
    }

    public void setContext(Repository context) {
        this.context = context;
    }
}

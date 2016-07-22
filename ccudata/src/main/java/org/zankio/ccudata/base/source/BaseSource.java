package org.zankio.ccudata.base.source;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Offline;
import org.zankio.ccudata.base.utils.AnnotationUtils;

import rx.Observable;

public abstract class BaseSource<TArgument, TData> {
    public final String TYPE;
    protected Repository context;

    protected BaseSource() {
        this.TYPE = getPrimaryType();
    }

    public SourceProperty.Level getOrder() {
        return SourceProperty.Level.LOW;
    }

    public void before(Request<TData, TArgument> request) {
        Observable.just(request)
                .compose(context.preProgressRequest())
                .toBlocking()
                .single();
    }

    public abstract TData fetch(Request<TData, TArgument> request) throws Exception;

    public void after(Response<TData, TArgument> response) {
        Observable.just(response)
                .compose(context.postProgressResponse())
                .toBlocking()
                .single();
    }

    public String[] getDataType() {
        return AnnotationUtils.getAnnotationValue(this.getClass(), DataType.class, new String[]{});
    }

    public String getPrimaryType() {
        String[] types = getDataType();
        return types.length > 0 ? types[0] : "";
    }

    public Repository getContext() {
        return context;
    }

    public BaseSource<TArgument, TData> setContext(Repository context) {
        this.context = context;
        return this;
    }

    public void init() {}

    public Boolean isOffline() {
        return AnnotationUtils.getAnnotationValue(this.getClass(), Offline.class, false);
    }
}

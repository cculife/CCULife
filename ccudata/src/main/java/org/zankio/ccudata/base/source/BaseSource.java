package org.zankio.ccudata.base.source;

import org.zankio.ccudata.base.Repository;
public abstract class BaseSource<TData> {
    protected Repository context;

    public SourceProperty.Level getOrder() {
        return SourceProperty.Level.LOW;
    }

    public void before() { }
    public abstract TData fetch() throws Exception;
    public void after() { }

    public abstract String getType();

    public Repository getContext() {
        return context;
    }

    public void setContext(Repository context) {
        this.context = context;
    }
}

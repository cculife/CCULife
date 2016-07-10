package org.zankio.ccudata.base.source;

public abstract class BaseSource<TData> {
    public SourceProperty.Level getOrder() {
        return SourceProperty.Level.LOW;
    }

    public void before() { }
    public abstract TData fetch() throws Exception;
    public void after() { }

    public abstract String getType();
}

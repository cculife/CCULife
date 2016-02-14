package org.zankio.cculife.CCUService.base.listener;

public abstract class BindParamOnUpdateListener<TData, TParam> extends OnUpdateListener<TData> {

    protected TParam parameter;

    public BindParamOnUpdateListener(TParam parameter) { this.parameter = parameter; }
    public BindParamOnUpdateListener(TParam parameter, IOnUpdateListener listener) {
        super(listener);
        this.parameter = parameter;
    }
}

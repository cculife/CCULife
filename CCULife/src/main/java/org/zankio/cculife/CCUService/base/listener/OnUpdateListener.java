package org.zankio.cculife.CCUService.base.listener;


import org.zankio.cculife.CCUService.base.source.BaseSource;

public abstract class OnUpdateListener<TData> implements IOnUpdateListener<TData> {

    private IOnUpdateListener<TData> listener;

    public OnUpdateListener() { }
    public OnUpdateListener(IOnUpdateListener<TData> listener) { this.listener = listener; }

    @Override
    public void onNext(String type, TData data, BaseSource source) {
        if (listener != null) listener.onNext(type, data, source);
    }

    @Override
    public void onComplete(String type) {
        if (listener != null) listener.onComplete(type);
    }

    @Override
    public void onError(String type, Exception err, BaseSource source) {
        if (listener != null) listener.onError(type, err, source);
    }
}

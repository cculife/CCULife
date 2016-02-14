package org.zankio.cculife.CCUService.base.listener;

import android.util.Log;

import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;

import java.util.ArrayList;

public class PriorityUpdateListener<T> implements IOnUpdateListener<T> {
    private ArrayList<IOnUpdateListener<T>> listeners;
    private SourceProperty.Level lastImportant;
    private SourceProperty.Level lastException;
    private Exception exception;
    private int remainSource;

    /*
        OrderLevel      High  Middle  Low
        ImportantLevel                      High    Middle  Low
        Data             -      -      -      *        -      -
        Exception        *      -      -      -        -      -
     */

    public PriorityUpdateListener(IOnUpdateListener<T> listener) {
        this.listeners = new ArrayList<>();
        this.listeners.add(listener);
        this.remainSource = 0;
    }

    public void addListener(IOnUpdateListener<T> ...listeners) {
        for (IOnUpdateListener<T> listener: listeners) {
            this.listeners.add(listener);
        }
    }

    private void callComplete(String type) {
        for (IOnUpdateListener<T> listener: this.listeners) {
            listener.onComplete(type);
        }
    }

    private void callNext(String type, T data, BaseSource source) {
        Log.d("source", source == null ? "null" : source.toString());
        for (IOnUpdateListener<T> listener: this.listeners) {
            listener.onNext(type, data, source);
        }
    }

    private void callError(String type, Exception err, BaseSource source) {
        for (IOnUpdateListener<T> listener: this.listeners) {
            listener.onError(type, err, source);
        }
    }

    @Override
    public void onNext(String type, T data, BaseSource source) {
        Log.d("onNext", source == null ? "null" : source.toString());
        remainSource --;

        if ((lastImportant == null || source.property.important.compareTo(lastImportant) > 0)) {
            exception = null;
            callNext(type, data, source);
            lastImportant = source.property.important;
            lastException = SourceProperty.Level.LOW;
        }

        if (remainSource == 0) callComplete(type);
    }

    @Override
    public void onComplete(String type) { }

    @Override
    public void onError(String type, Exception err, BaseSource source) {
        remainSource --;
        if (lastException == null || source.property.important.compareTo(lastException) < 0) {
            exception = err;
        }

        if (remainSource == 0) {
            if (exception != null) callError(type, exception, null);
            callComplete(type);
        }
    }

    public IOnUpdateListener<T> getListener() {
        this.remainSource ++;
        return this;
    }

}

package org.zankio.cculife.CCUService.base.listener;

import org.zankio.cculife.CCUService.base.source.BaseSource;

public interface IOnUpdateListener<TData> {

    void onNext(String type, TData data, BaseSource source);
    void onComplete(String type);
    void onError(String type, Exception err, BaseSource source);
}


/*

class FetchTask{
    BaseRepo context
    Object parameter
    Listener listener

    fetch: type, parameter
        sources = content.getSource(String type);
        context.fetch(type, parameter, listerer = this)

    cancel


    listener
        onNext
        onComplete
        onError
}

interface Listener {
    onData
    onError
}

interface Listener {
    onData
    onError
}

class Response {
    parameter
    response
}
*/
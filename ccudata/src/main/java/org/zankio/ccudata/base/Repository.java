package org.zankio.ccudata.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.base.model.Storage;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.base.source.SourceJar;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

// Bourse Base Class
public abstract class Repository {

    // Timeout Constant
    private static final int CONNECT_TIMEOUT = 15000;
    private Context context;

    private Storage storage = new Storage();

    private SourceJar sourceJar = new SourceJar();
    protected abstract BaseSource[] getSources();

    public Repository(Context context) {
        this.context = context;
        initialSource();
    }

    private void initialSource() {
        for (BaseSource source: getSources()) {
            source.setContext(this);
            for (String type: source.getDataType())
                sourceJar.addSource(type, source);
        }
    }

    public <TData, TArgument> Observable<Response> fetch(Request<TData, TArgument> request) {
        return Observable.just(request)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(sourceJar.mapAll())
                .compose(parallel(requestObservable -> requestObservable.compose(executeSource())))
                .compose(delayError())
                .compose(filterResult())
                .compose(bindListener(request.type));
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private <TData, TArgument>Observable.Transformer<Response<TData, TArgument>, Response<TData,TArgument>> delayError() {
        final Response[] last = {null};
        final boolean[] hasException = {true};

        return responseObservable -> responseObservable
                .doOnNext(response -> {
                    Log.d("Data", "delayError onNext" +response.exception());

                    if (response.exception() == null) {
                        hasException[0] = false;
                        return;
                    }

                    if (last[0] == null || new RequestOrderComparator().compare(response.args(), last[0].args()) > 0)
                        last[0] = response;
                })
                .filter(response -> response.exception() == null)
                .doOnCompleted(() -> {
                    if (hasException[0])
                        throw Exceptions.propagate(last[0].exception());
                });
    }


    private <TData, TArgument> Observable.Transformer<Response<TData, TArgument>, Response<TData, TArgument>> filterResult() {
        final Request[] last = new Request[]{null};

        return responseObservable ->
                // filter the lastest data
                responseObservable.filter(response -> new RequestOrderComparator().compare(response.args(), last[0]) <= 0 )
                // record last record
                .doOnNext(response -> last[0] = response.args())
                // clear memory
                .doOnCompleted(() -> last[0] = null);
    }

    private <T, R>Observable.Transformer<T, R> parallel(Func1<Observable<T>, Observable<R>> func1) {
        Log.d("DATA", "parallel");
        return requestObservable ->
                requestObservable.flatMap(
                        request -> func1.call(
                                Observable.just(request)
                                    .subscribeOn(Schedulers.computation())
                        )
                );
    }

    private <TData, TArgument> Observable.Transformer<Request<TData, TArgument>, Response<TData, TArgument>> executeSource() {
        Log.d("DATA", "executeSource");
        return requestObservable -> {
            Log.d("DATA", "executeSourceOB");
            return requestObservable
                    .map(request -> {
                        request.source().before(request);
                        return request;
                    })
                    .flatMap(request -> {
                        try {
                            Log.d("DATA", "source: " + request.source());
                            return Observable.just(
                                    new Response<>(request.target.cast(request.source().fetch(request)), request)
                            );
                        } catch (ClassCastException e) {
                            return Observable.just(
                                    new Response<>(new Exception("Result class not match", e), request)
                            );
                        } catch (Exception e) {
                            return Observable.just(
                                    new Response<>(e, request)
                            );
                        }
                    })
                    .map(response -> {
                        Request request = response.args();
                        if (request != null)
                            request.source().after(response);
                        return response;
                    });
        };
    }

    private Observable.Transformer<Response, Response> bindListener(String type) {
        Log.d("DATA", "bindListener");
        /*return observable -> {
            if (listener != null)
                for (IGetOnNext source : listener)
                    observable.doOnNext(source.call(type));

            return observable;
        };*/
        return responseObservable -> responseObservable;
    }

    public Storage storage() { return storage; }

    public Context getContext() { return context; }

    @NonNull
    public Observable.Transformer<Request, Request> preProgressRequest() {
        return requestObservable -> requestObservable;
    }

    @NonNull
    public Observable.Transformer<Response, Response> postProgressResponse() {
        return requestObservable -> requestObservable;
    }
}

package org.zankio.ccudata.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.base.model.Storage;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.base.source.SourceJar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

// Bourse Base Class
public abstract class Repository {

    public interface RequestTransformer<TData, TArgument>
            extends Observable.Transformer<Request<TData, TArgument>, Request<TData, TArgument>> { }

    public interface ResponseTransformer<TData, TArgument>
            extends Observable.Transformer<Response<TData, TArgument>, Response<TData, TArgument>> { }

    public interface GetListener extends Func0<Action1<Response>> {}

    // Timeout Constant
    private static final int CONNECT_TIMEOUT = 15000;
    private Context context;

    private Storage storage = new Storage();

    private SourceJar sourceJar = new SourceJar();
    private Map<String, Set<Func0<Action1<Response>>>> listenerSet = new HashMap<>();

    protected abstract BaseSource[] getSources();

    public Repository(Context context) {
        this.context = context;
        initialSource();
    }

    private void initialSource() {
        for (BaseSource source: getSources()) {
            source.setContext(this);
            source.init();
            for (String type: source.getDataType()) {
                sourceJar.addSource(type, source);
            }
        }
    }

    public <TData, TArgument> Observable<Response<TData, TArgument>> fetch(Request<TData, TArgument> request) {

        return Observable.just(request)
                .subscribeOn(Schedulers.io())
                .compose(sourceJar.mapAll())
                .compose(filterSource())
                .compose(parallel(requestObservable -> requestObservable.compose(executeSource())))
                .compose(delayError())
                .compose(filterResult())
                .compose(bindListener(request.type))
                .doOnError(Throwable::printStackTrace)
                .timeout(60, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread());
    }

    protected <TData, TArgument>RequestTransformer<TData, TArgument> filterSource() {
        return requestObservable -> requestObservable;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private <TData, TArgument>ResponseTransformer<TData, TArgument> delayError() {
        final Response[] last = {null};
        final boolean[] hasException = {true};

        return responseObservable -> responseObservable
                .doOnNext(response -> {
                    Log.d("Data", "delayError onNext" + response.exception());

                    if (response.exception() == null) {
                        hasException[0] = false;
                        return;
                    }

                    if (last[0] == null || new RequestOrderComparator().compare(response.request(), last[0].request()) > 0)
                        last[0] = response;
                })
                .filter(response -> response.exception() == null)
                .doOnCompleted(() -> {
                    if (hasException[0])
                        throw Exceptions.propagate(last[0].exception());
                });
    }


    private <TData, TArgument> ResponseTransformer<TData, TArgument> filterResult() {
        final Request[] last = new Request[]{null};

        return responseObservable ->
                // filter the lastest data
                responseObservable.filter(response -> new RequestOrderComparator().compare(response.request(), last[0]) <= 0 )
                // record last record
                .doOnNext(response -> last[0] = response.request())
                // clear memory
                .doOnCompleted(() -> last[0] = null);
    }

    private <T, R>Observable.Transformer<T, R> parallel(Func1<Observable<T>, Observable<R>> func1) {
        Log.d("DATA", "parallel");
        return requestObservable ->
                requestObservable.flatMap(
                        request -> func1.call(
                                Observable.just(request)
                                    .subscribeOn(Schedulers.io())
                        )
                );
    }

    private <TData, TArgument> Observable.Transformer<Request<TData, TArgument>, Response<TData, TArgument>> executeSource() {
        return requestObservable -> {
            Log.d("Repository", "executeSource");
            return requestObservable
                    .map(request -> {
                        Log.d("Repo executeSource", "Before " + request.source().getClass().getName());
                        try {
                            request.source().before(request);
                        } catch (Exception e) {
                            request.exception(e);
                        }
                        return request;
                    })
                    .flatMap(request -> {
                        Exception exception = request.exception();
                        if (exception != null) {
                            return Observable.just(
                                    new Response<>(exception, request)
                            );
                        }

                        try {
                            Log.d("Repo executeSource", "Execute source: " + request.source());
                            return Observable.just(
                                    new Response<>(request.target.cast(request.source().fetch(request)), request)
                            );
                        } catch (ClassCastException e) {
                            Log.d("Repo executeSource", "Class NotMetch");
                            return Observable.just(
                                    new Response<>(new Exception("Result class not match", e), request)
                            );
                        } catch (Exception e) {
                            Log.d("Repo executeSource", "Exception");
                            return Observable.just(
                                    new Response<>(e, request)
                            );
                        }
                    })
                    .map(response -> {
                        Log.d("Repo executeSource", "After " + response.request().source().getClass().getName());
                        Request<TData, TArgument> request = response.request();
                        if (request != null && request.exception() == null)
                            try {
                                request.source().after(response);
                            } catch (Exception ignored) {}
                        return response;
                    });
        };
    }

    private <TArgument, TData>ResponseTransformer<TData, TArgument> bindListener(String type) {
        Log.d("DATA", "bindListener");
        return observable -> {
            Set<Func0<Action1<Response>>> listeners = listenerSet.get(type);
            if (listeners != null)
                for (Func0<Action1<Response>> listener : listeners)
                    observable = observable.doOnNext(listener.call());

            return observable;
        };

        //return responseObservable -> responseObservable;
    }

    public void registeOnNext(String type, Func0<Action1<Response>> getOnNext) {
        Set<Func0<Action1<Response>>> listeners = listenerSet.get(type);
        if (listeners == null) {
            listeners = new HashSet<>();
            listenerSet.put(type, listeners);
        }

        listeners.add(getOnNext);
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

package org.zankio.ccudata.base.utils;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.ReplaySubject;

public class RxJavaUtils {
    public static <T> Observable.Transformer<T, T> cache() {
        return observable -> {
            ReplaySubject<T> subject = ReplaySubject.createWithSize(1);
            observable.subscribe(new Subscriber<T>() {
                @Override
                public void onCompleted() { subject.onCompleted(); }

                @Override
                public void onError(Throwable e) { subject.onError(e); }

                @Override
                public void onNext(T t) { subject.onNext(t); }
            });

            return subject;
        };
    }
}

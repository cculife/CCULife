package org.zankio.ccudata.base.source;

import android.util.Log;

import org.zankio.ccudata.base.model.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import rx.Observable;

public class SourceJar {
    protected HashMap<String, ArrayList<BaseSource>> sourceMap = new HashMap<>();

    public void addSource(String type, BaseSource source) {
        ArrayList<BaseSource> sources = sourceMap.get(type);
        if (sources == null) {
            sources = new ArrayList<>();
            sourceMap.put(type, sources);
        }

        sources.add(source);
    }

    public <TData, TArgument> Observable.Transformer<Request<TData, TArgument>, Request<TData, TArgument>> mapAll() {
        return requestObservable -> {
            return requestObservable.flatMap((Request<TData, TArgument> request) -> {
                Log.d("SourceJar", "mapAll: " + request.type);
                // get sources
                ArrayList<BaseSource> sources = sourceMap.get(request.type);

                if (sources == null)
                    return Observable.error(new Exception("No Source Found"));

                // sort source
                Collections.sort(sources, sourceComparator);

                Log.d("SourceJar", "mapAll: " + request.type + ", Count: " + sources.size());
                // map source to new request
                return Observable.from(sources)
                        .map(source -> new Request<>(request).source(source));
            });
        };
    }

    private static Comparator<BaseSource> sourceComparator =
            new SourcePropertyComparator();
}

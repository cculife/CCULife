package org.zankio.cculife.ui.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.HashMap;

public class CacheFragment extends Fragment {
    private final HashMap<String, Cache> hashMap;

    public CacheFragment() { hashMap = new HashMap<>(); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public static CacheFragment get(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        Fragment fragment;
        fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new CacheFragment();

            if (!fragmentManager.isDestroyed())
                fragmentManager.beginTransaction().add(fragment, tag).commit();
        }
        return (CacheFragment) fragment;
    }

    /*public <T>Observable<T> cache(final String key, Observable<T> observable) {
        return cache(key, observable, null, -1);
    }

    public <T>Observable<T> cache(final String key, Observable<T> observable, final int expired) {
        return cache(key, observable, null, expired);
    }

    public <T> Observable<T> cache(final String key, Observable<T> observable, Class<T> mClass) {
        return cache(key, observable, mClass, -1);
    }

    public <T>Observable<T> cache(final String key, Observable<T> observable, Class<T> mClass, final int expired) {
        BehaviorSubject<T> subject = BehaviorSubject.create();

        T data;
        if (mClass != null) {
            data = get(key, mClass);
            if (data != null) {
                subject.onNext(null, data, null);
                return null;
            }
        }

        set(key, o, expired);
        return subject.startWith(observable);
    }*/

    public <T>T get(String key, Class<? extends T> mClass) {
        Cache cache = hashMap.get(key);
        if (cache == null || (cache.expiredTime >=0 && cache.expiredTime < System.currentTimeMillis())) {
            hashMap.remove(key);
            return null;
        }
        return mClass.cast(cache.data);
    }

    public <T>void set(String key, T object) {
        hashMap.put(key, new Cache(object, -1));
    }

    public <T>void set(String key, T object, int expired) {
        hashMap.put(key, new Cache(object, System.currentTimeMillis() + expired));
    }

    public class Cache {
        public final long expiredTime;
        public final Object data;

        public Cache(Object data, long expiredTime) {
            this.data = data;
            this.expiredTime = expiredTime;
        }
    }
}

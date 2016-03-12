package org.zankio.cculife.CCUService.base;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.zankio.cculife.CCUService.base.listener.IGetListener;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.listener.PriorityUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public abstract class BaseRepo<TIdentify> {

    private static final int CONNECT_TIMEOUT = 15000;
    protected HashMap<String, ArrayList<BaseSource<Object>>> sourceMap;
    protected HashMap<String, ArrayList<IGetListener>> listenerGetter;

    private BaseSession<TIdentify> session;
    private Context context;

    public BaseRepo(Context context) {
        this.context = context;
        this.listenerGetter = new HashMap<>();
        this.sourceMap = new HashMap<>();
        initialSource();
    }

    public Connection buildConnection(String url) {
        Connection connection = Jsoup.connect(url);
        connection.timeout(CONNECT_TIMEOUT);
        if (session != null)
            session.buildConnection(connection);

        return connection;
    }

    protected void initialSource() {
        sourceMap.clear();
        for (BaseSource source: getSources()) {
            if (!this.filterSource(source)) continue;
            source.init();
        }
    }

    protected boolean filterSource(BaseSource source) { return true; }

    protected abstract BaseSource[] getSources();

    public void registerSource(BaseSource source, String type) {
        if (sourceMap.get(type) == null)
            sourceMap.put(type, new ArrayList<BaseSource<Object>>());

        ArrayList<BaseSource<Object>> list = sourceMap.get(type);
        list.add(source);
    }

    public Context getContext() { return this.context; }

    public BaseSession<TIdentify> getSession() {
        return this.session;
    }

    public void setSession(BaseSession<TIdentify> session) {
        this.session = session;
    }

    public void registerUpdateListener(IGetListener listener, String type) {
        if (listenerGetter.get(type) == null)
            listenerGetter.put(type, new ArrayList<IGetListener>());

        ArrayList<IGetListener> list = listenerGetter.get(type);
        list.add(listener);
    }

    public void unregisterUpdateListener(IGetListener listener, String type) {
        if (listenerGetter.get(type) == null)
            return;

        ArrayList<IGetListener> list = listenerGetter.get(type);
        list.remove(listener);
    }

    public <T>AsyncTask[] fetch(String type, IOnUpdateListener<T> listener, Object... arg) {
        Log.d("BaseRepo", String.format("fetch %s", type));

        ArrayList<BaseSource<Object>> sources = sourceMap.get(type);
        PriorityUpdateListener<T> priorityUpdateListener = new PriorityUpdateListener<>(listener);
        if (sources == null || sources.size() == 0) {
            listener.onError(type, new Exception("Source miss"),  null);
            return null;
        }

        Collections.sort(sources, sourceComparator);
        ArrayList<AsyncTask> tasks = new ArrayList<>();
        ArrayList<IGetListener> list = listenerGetter.get(type);
        if (list != null)
            for (IGetListener l : list) {
                priorityUpdateListener.addListener(l.getListener(type, arg));
            }

        for (BaseSource<Object> source: sources) {
            tasks.add(new FetchAsyncTask(type, source, priorityUpdateListener.getListener()).start(arg));
        }

        return tasks.toArray(new AsyncTask[tasks.size()]);
    }

    public Object fetchSync(String type, Object ...arg) throws Exception {
        Log.d("BaseRepo", String.format("fetch Sync %s", type));
        ArrayList<BaseSource<Object>> sources = sourceMap.get(type);
        if (sources != null) {
            for (BaseSource<Object> source: sources) {
                return source.fetch(type, arg);
            }
        }
        return null;
    }

    private static Comparator<BaseSource> sourceComparator = new Comparator<BaseSource>() {
        @Override
        public int compare(BaseSource l, BaseSource r) {
            return r.property.order.compareTo(l.property.order);
        }
    };


    // Fetch For Single Source
    public class FetchAsyncTask extends AsyncTaskWithErrorHanding<Object, Object, Object> {
        private String type;
        private BaseSource<Object> source;
        private IOnUpdateListener listener;

        public FetchAsyncTask(String type, BaseSource<Object> source, IOnUpdateListener listener) {
            this.type = type;
            this.source = source;
            this.listener = listener;
        }

        @Override
        protected void onError(Exception e, String msg) {
            listener.onError(type, e, source);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object _doInBackground(Object... params) throws Exception {
            return source.fetch(type, params);
        }

        @Override
        protected void _onPostExecute(Object result){
            this.listener.onNext(type, result, source);
        }

        public AsyncTask<Object, Object, Object> start(Object... params) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            } else {
                this.execute(params);
            }
            return this;
        }
    }

}

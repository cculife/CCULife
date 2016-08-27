package org.zankio.cculife.override;

import android.os.AsyncTask;

public abstract class AsyncTaskWithErrorHanding<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected Exception exception;

    protected abstract Result _doInBackground(Params... params) throws Exception;

    protected void onError(Exception e, String msg){ }
    protected void _onPostExecute(Result result) {}

    @Override
    protected void onPreExecute() {
        exception = null;
    }

    @Override
    protected Result doInBackground(Params... params) {
        try {
            return _doInBackground(params);
        } catch (InterruptedException e) {}
          catch (Exception e) { exception = e;}
        return null;
    }

    @Override
    protected void onPostExecute(Result result) {
        if(exception == null) _onPostExecute(result);
        else {
            exception.printStackTrace();
            onError(exception, exception.getMessage());
        }
    }
}

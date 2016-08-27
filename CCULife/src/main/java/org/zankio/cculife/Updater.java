package org.zankio.cculife;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.Net;
import org.zankio.cculife.override.NetworkErrorException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Updater {
    private final Context context;
    private Version version;
    private static final String UPDATE_URL = "http://cculife.herokuapp.com/update.html";
    private static final String DOWNLOAD_DIRPATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/org.zankio.cculife";
    private static final String DOWNLOAD_FULLPATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/org.zankio.cculife/CCULife.apk";
    public Updater(Context context) {
        this.context = context;
    }

    public Version getUpdate() throws NetworkErrorException { return getUpdate(false);}

    public Version getUpdate(boolean force) throws NetworkErrorException {
        SharedPreferences preferences;
        Version result = getLatestVersion();

        if (result != null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (force ||
                  (result.versionCode > 0 && result.versionCode > getVersionCode() && result.versionCode > preferences.getInt("update_ignore", 0)) ||
                  (preferences.getBoolean("debug_force_update", false))) {
                return result;
            }
        }
        return null;
    }

    public void checkUpdate() {
        SharedPreferences preferences;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean("auto_check_update", true)) {
            long latestCheck = preferences.getLong("update_latest_check", 0);
            int updateInterval = Integer.valueOf(preferences.getString("update_interval", "1"));

            if ((System.currentTimeMillis() - latestCheck > updateInterval * 24 * 60 * 60 * 1000)
                    || (preferences.getBoolean("debug_force_update", false))) {
                checkUpdate(true);
            }
        }
    }

    public void checkUpdate(boolean force) {
        if(!force) {checkUpdate(); return;}

        //new UpdateTask().execute();
    }

    public Dialog.OnClickListener dialogOnClick = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            SharedPreferences preferences;
            SharedPreferences.Editor editor;
            ProgressDialog mProgressDialog;

            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            editor = preferences.edit();

            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if(version != null) {
                        mProgressDialog = new ProgressDialog(context);
                        mProgressDialog.setMessage("下載更新中...");
                        mProgressDialog.setIndeterminate(true);
                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgressDialog.setCancelable(true);

                        final DownloadTask downloadTask = new DownloadTask(context, mProgressDialog);
                        downloadTask.execute(version.downloadURL);

                        /*
                        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                downloadTask.cancel(true);
                            }
                        });
                        */

                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    if(version != null) {
                        editor.putInt("update_ignore", version.versionCode);
                        editor.apply();
                    }

                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    dialog.dismiss();
                    break;
            }
        }
    };

    /*private class UpdateTask extends AsyncTaskWithErrorHanding<Void, Void, Version> {

        @Override
        protected void onError(Exception e, String msg) {
            super.onError(e, msg);
            Toast.makeText(context, "檢查更新錯誤: " + msg, Toast.LENGTH_LONG).show();
        }

        @Override
        protected Version _doInBackground(Void... params) throws Exception {
            return getUpdate();
        }

        @Override
        protected void _onPostExecute(Version version) {
            SharedPreferences preferences;
            SharedPreferences.Editor editor;

            //AlertDialog dialog;
            AlertDialog.Builder builder;

            Updater.this.version = version;

            if(version != null) {
                builder = new AlertDialog.Builder(context);
                builder.setTitle("發現更新");
                builder.setMessage(version.versionName + "\n\n" + version.changelog);
                builder.setPositiveButton("更新", dialogOnClick);
                builder.setNegativeButton("不再提醒", dialogOnClick);
                builder.setNeutralButton("暫時不要", dialogOnClick);
                builder.show();
            } else {
                preferences = PreferenceManager.getDefaultSharedPreferences(context);
                editor = preferences.edit();
                editor.putLong("update_latest_check", System.currentTimeMillis());
                editor.apply();
            }
        }
    }*/

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private ProgressDialog mProgressDialog;

        public DownloadTask(Context context, ProgressDialog mProgressDialog) {
            this.context = context;
            this.mProgressDialog = mProgressDialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "下載更新檔案錯誤: " + result, Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(context, "下載更新檔案完成", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(DOWNLOAD_FULLPATH)), "application/vnd.android.package-archive");
                context.startActivity(intent);
            }
        }

        @Override
        protected String doInBackground(String... sUrl) {
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download

            PowerManager pm;
            PowerManager.WakeLock wl;

            pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wl.acquire();

            try {
                int responseCode;
                String redirectUrl;
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(sUrl[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setInstanceFollowRedirects(false);
                    connection.connect();

                    while ((responseCode = connection.getResponseCode()) == HttpURLConnection.HTTP_MOVED_PERM ||
                            responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                        redirectUrl = connection.getHeaderField("Location");
                        if(redirectUrl == null) {
                            return "Can't find redirect url";
                        }

                        url = new URL(redirectUrl);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setInstanceFollowRedirects(false);
                        connection.connect();

                    }

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                        return "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage();

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();

                    // download the file
                    input = connection.getInputStream();
                    new File(DOWNLOAD_DIRPATH).mkdirs();
                    output = new FileOutputStream(DOWNLOAD_FULLPATH);

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled())
                            return null;
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }
                } catch (Exception e) {
                    return e.toString();
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    }
                    catch (IOException ignored) { }

                    if (connection != null)
                        connection.disconnect();
                }
            } finally {
                wl.release();
            }
            return null;
        }
    }

    public Version getLatestVersion() throws NetworkErrorException {
        Connection connection;
        Document document;
        Element latest;
        Version result = new Version();

        try {
            connection = Jsoup.connect(UPDATE_URL).timeout(Net.CONNECT_TIMEOUT);

            document = connection.get();
            latest = document.getElementById("latest_version");

            if(latest == null) return null;

            result.versionCode = elementsToInt(latest.getElementsByClass("versionCode"));
            result.versionName = elementsToString(latest.getElementsByClass("versionName"));
            result.downloadURL = elementsToString(latest.getElementsByClass("downloadURL_short"));
            result.changelog = elementsToString(latest.getElementsByClass("changelog"));
            if (result.downloadURL == null) elementsToString(latest.getElementsByClass("downloadURL"));

            return result;
        } catch (IOException e) {
            throw Exceptions.getNetworkException(e);
        }
    }

    public String elementsToString(Elements elements) {
        if(elements != null && elements.size() > 0) {
            return elements.text().replace("\\n", "\n");
        }
        return null;
    }

    public int elementsToInt(Elements elements) {
        if(elements != null && elements.size() > 0) {
            try {
                return Integer.parseInt(elements.text());
            } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    public int getVersionCode() {
        int versionCode = -1;
        PackageManager pm;
        PackageInfo info;

        try {
            if(context != null) {
                pm = context.getPackageManager();
                if (pm != null) {
                    info = pm.getPackageInfo(context.getPackageName(), 0);
                    versionCode = info.versionCode;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    class Version {
        String downloadURL;
        String changelog;
        String versionName;
        int versionCode;
    }
}

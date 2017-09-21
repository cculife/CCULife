package org.zankio.cculife.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.zankio.cculife.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class DownloadService extends IntentService {
    public enum State { Downloading, Finished, Error}
    private final static String TAG = "DownloadService";
    private final static String SSL_TEST_URL = "https://ecourse.ccu.edu.tw/robots.txt";
    static final ArrayList<Integer> notifyID = new ArrayList<>();
    static TrustManagerFactory tmf;
    static int total = 0;

    public DownloadService() {
        super(TAG);
    }

    public boolean checkSSL() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preference.edit();

        int mode = preference.getInt("SSL_MODE", 0);
        if (mode == 1) return true;
        else if (mode == 2) return false;
        try {
            Ion.with(this).load(SSL_TEST_URL).asString().get();
            editor.putInt("SSL_MODE", 1);
            editor.apply();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            if ("javax.net.ssl.SSLException".equals(e.getMessage())) {
                editor.putInt("SSL_MODE", 2);
                editor.apply();
                return false;
            }
            e.printStackTrace();
        }
        return true;
    }

    public static void downloadFile(Context context, String url, String filename) {
        Intent download = new Intent(context, DownloadService.class);
        Bundle data = new Bundle();
        data.putString("url", url);
        data.putString("filename", filename);
        download.putExtras(data);

        context.startService(download);
    }


    private PendingIntent generateOpenFilePendingIntent(int id, String path, String filename, String state) {
        Bundle data;
        Intent notifyIntent;

        notifyIntent = new Intent(this, FileOpenService.class);

        data = new Bundle();
        data.putInt("id", id);
        data.putString("TAG", TAG);
        data.putString("path", path);
        data.putString("filename", filename);
        data.putString("state", state);

        notifyIntent.putExtras(data);
        if ("error".equals(state)) id = (id + 1) * -1;
        return PendingIntent.getService(this, id, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    protected void onHandleIntent(Intent intent) {
        final NotificationManager mNotifyManager;
        final NotificationCompat.Builder mBuilder;
        final PendingIntent notifyFinishIntent;
        final PendingIntent notifyErrorIntent;
        final int currentId = total++;
        notifyID.add(currentId);

        Bundle data;

        String filename, url;
        File path;
        initSSL();

        data = intent.getExtras();
        filename = data.getString("filename");
        url = data.getString("url");

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (!path.exists())
            path.mkdir();

        mBuilder = new NotificationCompat.Builder(this);
        DownloadService.notify(this, mNotifyManager, mBuilder, currentId, filename);
        notifyFinishIntent = generateOpenFilePendingIntent(currentId, path.getAbsolutePath(), filename, "finish");
        notifyErrorIntent = generateOpenFilePendingIntent(currentId, path.getAbsolutePath(), filename, "error");

        try {
            Ion.with(this)
                .load(url)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        DownloadService.notify(DownloadService.this, mNotifyManager, mBuilder, currentId, (int) total, (int) downloaded);
                    }
                })
                .write(new File(path, filename))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File file) {
                        if (e != null) {
                            e.printStackTrace();
                            DownloadService.notify(DownloadService.this, mNotifyManager, mBuilder, currentId, State.Error, notifyErrorIntent);
                        } else
                            DownloadService.notify(DownloadService.this, mNotifyManager, mBuilder, currentId, State.Finished, notifyFinishIntent);
                    }
                }).get();
        } catch (Exception e) {
            e.printStackTrace();
            DownloadService.notify(this, mNotifyManager, mBuilder, currentId, State.Error, notifyErrorIntent);
        }
    }
    private static void notify (Context context, NotificationManager mNotifyManager, NotificationCompat.Builder builder, int id, String filename) {
        builder.setContentTitle(context.getResources().getString(R.string.download_file) + filename)
                .setContentText(context.getResources().getString(R.string.downloading))
                .setOngoing(true)
                .setSmallIcon(getNotificationIcon());
        builder.setColor(ContextCompat.getColor(context, R.color.accent));
        builder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT));
        mNotifyManager.notify(TAG, id, builder.build());
    }

    private static void notify(Context context, NotificationManager mNotifyManager, NotificationCompat.Builder builder, int id, int total, int downloaded) {
        builder.setProgress(total, downloaded, false);
        mNotifyManager.notify(TAG, id, builder.build());
    }
    private static void notify(Context context, NotificationManager mNotifyManager, NotificationCompat.Builder builder, int id, DownloadService.State state) {
        notify(context, mNotifyManager, builder, id, state, null);
    }
    private static void notify(Context context, NotificationManager mNotifyManager, NotificationCompat.Builder builder, int id, DownloadService.State state, PendingIntent pendingIntent) {
        switch (state) {
            case Finished:
                builder.setContentText(context.getResources().getString(R.string.download_complete));
                break;
            case Error:
                builder.setContentText(context.getResources().getString(R.string.download_error));
                break;
        }
        builder.setSmallIcon(getNotificationIcon());
        builder.setColor(ContextCompat.getColor(context, R.color.accent));
        builder.setProgress(0, 0, false);
        builder.setOngoing(false);
        if (pendingIntent != null) builder.setContentIntent(pendingIntent);
        mNotifyManager.cancel(TAG, id);
        mNotifyManager.notify(TAG, id, builder.build());
        notifyID.remove(Integer.valueOf(id));
    }

    private static int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.white_icon : R.drawable.sicon;
    }

    @Override
    public void onDestroy() {
        NotificationManager mNotifyManager;
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        for(int id: notifyID) {
            mNotifyManager.cancel(TAG, id);
        }
    }

    public void initSSL() {

        try {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(this.getAssets().open("ecourse_ssl.crt"));
            Certificate ca;

            //noinspection TryFinallyCanBeTryWithResources
            try {
                ca = cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext ssl_context = SSLContext.getInstance("TLS");
            ssl_context.init(null, tmf.getTrustManagers(), null);

            Ion.getDefault(this).getHttpClient().getSSLSocketMiddleware().setTrustManagers(tmf.getTrustManagers());
            if(!checkSSL()) Ion.getDefault(this).getHttpClient().getSSLSocketMiddleware().setSSLContext(ssl_context);


        } catch (Exception ignored) { }
    }

}

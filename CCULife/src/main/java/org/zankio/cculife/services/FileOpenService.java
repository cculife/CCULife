package org.zankio.cculife.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.zankio.cculife.R;

import java.io.File;

public class FileOpenService extends IntentService {
    final Handler handler;
    public FileOpenService() {
        super("FileOpenService");
        handler = new Handler();
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationManager mNotifyManager;
        MimeTypeMap map;


        map = MimeTypeMap.getSingleton();
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Bundle data = intent.getExtras();
        String state = data.getString("state");
        String filename = data.getString("filename");
        String path = data.getString("path");
        int id = data.getInt("id");
        String TAG = data.getString("TAG");

        File file = new File(path, filename);
        if (!"error".equals(state) && file.exists()) {
            String[] names = file.getName().split("\\.");
            String ext;
            if (names.length > 0)
                ext = names[names.length - 1];
            else
                ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());

            String type = map.getMimeTypeFromExtension(ext);

            if (type == null)
                type = "*/*";

            intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //intent.setDataAndType(Uri.fromFile(file), type);
            intent.setDataAndType(FileProvider.getUriForFile(this, "org.zankio.cculife.provider", file), type);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                handler.post(() -> Toast.makeText(this, R.string.cant_open, Toast.LENGTH_SHORT).show());
            }
        }

        mNotifyManager.cancel(TAG, id);

    }
}

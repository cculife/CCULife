package org.zankio.cculife.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

public class PermissionUtils {
    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 0;
    public static boolean checkPermission (Activity activity, String permission, int code) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, code);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, code);
            }
            return false;
        }
        return true;
    }

    public static boolean checkPermission (Fragment fragment, String permission, int code) {
        if (ContextCompat.checkSelfPermission(fragment.getActivity(), permission)
                != PackageManager.PERMISSION_GRANTED) {

            if (fragment.shouldShowRequestPermissionRationale(permission)) {
                fragment.requestPermissions(new String[]{permission}, code);
            } else {
                fragment.requestPermissions(new String[]{permission}, code);
            }
            return false;
        }
        return true;
    }

    public static boolean checkWritePermission(Activity activity) {
        return checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }

    public static boolean checkWritePermission(Fragment fragment) {
        return checkPermission(fragment, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }
}

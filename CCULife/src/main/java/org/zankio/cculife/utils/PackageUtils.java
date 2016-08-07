package org.zankio.cculife.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageUtils {
    public static String getVersionName(Context context) {
        PackageManager pm;
        PackageInfo pinfo;

        try {
            pm = context.getPackageManager();
            if(pm != null) {
                pinfo = pm.getPackageInfo(context.getPackageName(), 0);
                return pinfo.versionName;
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

}

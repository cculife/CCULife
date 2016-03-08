package org.zankio.cculife;

import android.util.Log;

public class Debug {
    public static final String TAG = "CCULife.Debug";
    public static boolean debug = false;
    public static final boolean log = false;
    public static void e(String msg) {
        if (log) Log.e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (log) Log.e(tag, msg);
    }

    public static void d(String msg) {
        if (log) Log.d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (log) Log.d(TAG, msg);
    }
}

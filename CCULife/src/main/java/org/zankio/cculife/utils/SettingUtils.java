package org.zankio.cculife.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.zankio.ccudata.base.model.OfflineMode;

public class SettingUtils {
    public static OfflineMode loadOffline(Context context) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        return OfflineMode.values()[
                    Integer.valueOf(
                            preference.getString(
                                    "offline_mode",
                                    String.valueOf(OfflineMode.CLASS.ordinal())
                            )
                    )
                ];
    }
}

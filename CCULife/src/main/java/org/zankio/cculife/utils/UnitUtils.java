package org.zankio.cculife.utils;

import android.content.Context;
import android.util.TypedValue;

public class UnitUtils {
    public static final int getDp(Context context, int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
    }
}

package org.zankio.cculife.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import org.zankio.cculife.R;
import org.zankio.cculife.utils.CourseUtils;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class UpdateCoursePreference extends DialogPreference {
    CourseUtils instance = CourseUtils.getInstance(getContext());

    public UpdateCoursePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogMessage("確定要更新?");
        setPositiveButtonText(R.string.ok);
        setNegativeButtonText(R.string.cancel);
        setTitle(R.string.update_course_list);
        setDialogIcon(null);
    }

    @Override
    public CharSequence getSummary() {
        super.getSummary();
        if (instance.isUpdating()) return getContext().getString(R.string.updating);

        Long time = getPersistedLong(0);
        if (time == 0) return null;

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(calendar.getTime());

        return getContext().getString(R.string.last_update_at) + date;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            if (!instance.isUpdating()) {
                instance.setOnUpdateStateChangeListener(state -> {
                    switch (state) {
                        case UPDATING:
                            setSummary(R.string.updating);
                            break;
                        case SUCCESS:
                            setSummary(R.string.update_success);
                            break;
                        case FAIL:
                            setSummary(R.string.update_fail);
                    }
                });
                instance.updateCourse();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }
}

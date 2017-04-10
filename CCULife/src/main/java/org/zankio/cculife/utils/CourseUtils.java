package org.zankio.cculife.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.kiki.Kiki;
import org.zankio.ccudata.kiki.source.remote.AllCourseSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.GregorianCalendar;

import rx.Subscriber;

public class CourseUtils {
    public static final String COURSE_LIST_TMP = "course_list_tmp";
    public static final String COURSE_LIST_OLD = "course_list_old";
    public static final String COURSE_LIST = "course_list";

    public enum UpdateState { SUCCESS, FAIL, UPDATING }
    public interface onUpdateStateChangeListener { void onChange(UpdateState state); }

    private onUpdateStateChangeListener onUpdateStateChangeListener;
    private static CourseUtils instance;
    private boolean updating;
    private Kiki kiki;
    private Context context;
    private CourseUtils(Context context) {
        if (context == null) throw new IllegalArgumentException();
        this.kiki = new Kiki(context);
        this.context = context;
    }

    public void setOnUpdateStateChangeListener(CourseUtils.onUpdateStateChangeListener onUpdateStateChangeListener) {
        this.onUpdateStateChangeListener = onUpdateStateChangeListener;
    }

    public boolean isUpdating() {
        return updating;
    }

    public static CourseUtils getInstance(Context context) {
        if (instance == null) instance = new CourseUtils(context);
        return instance;
    }

    public static String getCourseList(Context context) {
        try {
            FileInputStream fileInputStream = context.openFileInput(COURSE_LIST);
            int length = fileInputStream.available();
            byte[] buffer = new byte[length];
            fileInputStream.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateCourse() {
        kiki.fetch(AllCourseSource.request())
                .subscribe(new Subscriber<Response<String, Void>>() {
                    @Override
                    public void onStart() {
                        updating = true;

                        if (onUpdateStateChangeListener != null)
                            onUpdateStateChangeListener.onChange(UpdateState.UPDATING);
                    }

                    @Override
                    public void onCompleted() {
                        updating = false;

                        if (onUpdateStateChangeListener != null)
                            onUpdateStateChangeListener.onChange(UpdateState.SUCCESS);
                    }

                    @Override
                    public void onError(Throwable e) {
                        updating = false;

                        if (onUpdateStateChangeListener != null)
                            onUpdateStateChangeListener.onChange(UpdateState.FAIL);
                    }

                    @Override
                    public void onNext(Response<String, Void> response) {
                        try {
                            boolean success = true;

                            String data = response.data();
                            FileOutputStream output = context.openFileOutput(COURSE_LIST_TMP, Context.MODE_PRIVATE);
                            output.write(data.getBytes());
                            File filesDir = context.getFilesDir();
                            File oldFile = new File(filesDir, COURSE_LIST_OLD);
                            File tmpFile = new File(filesDir, COURSE_LIST_TMP);
                            File curFile = new File(filesDir, COURSE_LIST);

                            if (oldFile.exists()) success = oldFile.delete();
                            if (!success) return;

                            if (curFile.exists()) success = curFile.renameTo(oldFile);
                            if (!success) return;

                            success = tmpFile.renameTo(curFile);
                            if (!success) {
                                success = oldFile.renameTo(curFile);
                                if (!success) {
                                    curFile.delete();
                                    oldFile.delete();
                                    tmpFile.delete();
                                }
                                return;
                            }

                            tmpFile.delete();

                            SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
                            preferences.putLong("update_course_list", new GregorianCalendar().getTimeInMillis());
                            preferences.apply();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


}

package org.zankio.cculife.ui.CourseSchedule;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.listener.OnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.kiki.Kiki;
import org.zankio.cculife.CCUService.kiki.model.TimeTable;
import org.zankio.cculife.CCUService.kiki.source.local.DatabaseTimeTableSource;
import org.zankio.cculife.CCUService.kiki.source.remote.TimetableSource;

import java.util.HashSet;

public class TimetableDataFragment extends Fragment
        implements IGetTimeTableData, IOnUpdateListener<TimeTable>, IGetListener<TimeTable>, IGetInteger {
    private static final String TAG_TIMETABLE_DATA_FRAGMENT = "TIMETABLE_DATA_FRAGMENT";
    public static final String LAST_PAGE = "LAST_PAGE";
    private Kiki kiki;
    private boolean loading;
    private HashSet<IOnUpdateListener<TimeTable>> listeners = new HashSet<>();
    private TimeTable timeTable;
    private int lastPage;

    public void init(Context context) {
        if (kiki == null)
            kiki = new Kiki(context);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        kiki = new Kiki(getContext());
    }

    @Override
    public boolean getTimeTable(IOnUpdateListener<TimeTable> listener) {
        if (this.timeTable != null) {
            listener.onNext(TimetableSource.TYPE, this.timeTable, null);
            return false;
        }
        listeners.add(listener);
        if (loading) {
            //Todo Race condition ?
            return true;
        }
        loading = true;
        kiki.fetch(TimetableSource.TYPE, this);
        return true;
    }

    private IOnUpdateListener localCourseListener = new OnUpdateListener<TimeTable>() {
        @Override
        public void onNext(String type, TimeTable o, BaseSource source) {
            super.onNext(type, o, source);
            if (o == null) return;
            if (TimetableDataFragment.this.timeTable == null) return;

            TimetableDataFragment.this.timeTable.mergeTimetable(o);
            TimetableDataFragment.this.onNext(TimetableSource.TYPE, TimetableDataFragment.this.timeTable, null);
        }
    };

    @Override
    public void onNext(String type, TimeTable timeTable, BaseSource source) {
        loading = false;
        this.timeTable = timeTable;
        for (IOnUpdateListener<TimeTable> listener: listeners)
            listener.onNext(type, timeTable, source);

        if (source != null)
            kiki.fetch(DatabaseTimeTableSource.TYPE_USERADD, localCourseListener);
    }

    @Override
    public void onError(String type, Exception err, BaseSource source) {
        loading = false;
        for (IOnUpdateListener listener: listeners)
            listener.onError(type, err, source);

    }

    @Override
    public void onComplete(String type) {
        for (IOnUpdateListener listener: listeners)
            listener.onComplete(type);
    }

    public static TimetableDataFragment getFragment(FragmentManager fragmentManager) {
        return getFragment(fragmentManager, false);
    }

    public static TimetableDataFragment getFragment(FragmentManager fragmentManager, boolean raw) {
        TimetableDataFragment fragment;
        fragment = (TimetableDataFragment) fragmentManager.findFragmentByTag(TAG_TIMETABLE_DATA_FRAGMENT);
        if (!raw && fragment == null) {
            fragment = new TimetableDataFragment();
            fragmentManager.beginTransaction().add(fragment, TAG_TIMETABLE_DATA_FRAGMENT).commitAllowingStateLoss();
        }
        return fragment;
    }

    @Override
    public IOnUpdateListener<TimeTable> getUpdateListener() {
        return this;
    }

    @Override
    public void registerListener(IOnUpdateListener<TimeTable> listener) {
        if (listeners == null) listeners = new HashSet<>();
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(IOnUpdateListener<TimeTable> listener) {
        if (listeners == null) return ;
        listeners.remove(listener);
    }

    @Override
    public int getInt(String key) {
        return lastPage;
    }

    @Override
    public void setInt(String key, int value) {
        lastPage = value;
    }
}

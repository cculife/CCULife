package org.zankio.cculife.ui.course.schedule;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.zankio.ccudata.base.model.OfflineMode;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.base.model.Storage;
import org.zankio.ccudata.kiki.Kiki;
import org.zankio.ccudata.kiki.model.SemesterData;
import org.zankio.ccudata.kiki.model.TimeTable;
import org.zankio.ccudata.kiki.source.local.DatabaseTimeTableSource;
import org.zankio.ccudata.kiki.source.remote.TimetableSource;
import org.zankio.cculife.UserManager;
import org.zankio.cculife.ui.base.GetStorage;
import org.zankio.cculife.utils.SettingUtils;

import rx.Subscriber;
import rx.subjects.ReplaySubject;

public class TimetableDataFragment extends Fragment
        implements IGetTimeTableData, GetStorage{
    private static final String TAG_TIMETABLE_DATA_FRAGMENT = "TIMETABLE_DATA_FRAGMENT";
    public static final String LAST_PAGE = "LAST_PAGE";
    private Kiki kiki;

    private ReplaySubject<TimeTable> subject;
    private final Storage storage = new Storage();

    public void init(Context context) {
        if (kiki == null) {
            UserManager userManager = UserManager.getInstance(context);
            OfflineMode offlineMode = SettingUtils.loadOffline(context);

            kiki = new Kiki(context);
            kiki.setOfflineMode(offlineMode).user()
                    .username(userManager.getUserName())
                    .password(userManager.getPassword());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public ReplaySubject<TimeTable> getTimeTable() {
        if (this.subject != null) {
            return this.subject;
        }
        subject = ReplaySubject.createWithSize(1);

        kiki.fetch(TimetableSource.request()).subscribe(
                new Subscriber<Response<TimeTable, SemesterData>>() {
                    @Override
                    public void onCompleted() { subject.onCompleted(); }

                    @Override
                    public void onError(Throwable e) { subject.onError(e); }

                    @Override
                    public void onNext(Response<TimeTable, SemesterData> response) {
                        TimeTable timetable = response.data();
                        subject.onNext(timetable);
                        kiki.fetch(new Request<>(DatabaseTimeTableSource.TYPE_USERADD, null, TimeTable.class))
                                .subscribe(res -> {
                                    TimeTable timetableUser = res.data();
                                    if (timetable == null) return;
                                    if (timetableUser == null) return;

                                    timetable.mergeTimetable(timetableUser);
                                    subject.onNext(timetable);
                                });
                    }
                });

        return subject;
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
    public Storage storage() { return storage; }

}

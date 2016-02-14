package org.zankio.cculife.CCUService.ecourse;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.authentication.CookieAuth;
import org.zankio.cculife.CCUService.base.constant.OfflineMode;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.ecourse.source.local.DatabaseAnnounceSource;
import org.zankio.cculife.CCUService.ecourse.source.local.DatabaseCourseListSource;
import org.zankio.cculife.CCUService.ecourse.source.local.DatabaseScoreSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.AnnounceContentSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.AnnounceSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.Authenticate;
import org.zankio.cculife.CCUService.ecourse.source.remote.ChangeCourseSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.ClassmateSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.CourseListSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.CustomCourseListSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.FileGroupFilesSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.FileGroupSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.FileSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.HomeworkContentSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.HomeworkSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.RollCallSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.ScoreSource;
import org.zankio.cculife.UserManager;

public class Ecourse extends BaseRepo<String> {
    private OfflineMode offline_mode;
    private UserManager userManager;

    public Ecourse(Context context) {
        super(context);
        setSession(new BaseSession<String>(new CookieAuth()));
    }

    @Override
    protected void initialSource() {
        loadPreferences();
        super.initialSource();
    }

    private void loadPreferences() {
        this.userManager = UserManager.getInstance(getContext());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (userManager.isSave() && preferences.getBoolean("offline_enable", true)) {
            int offline = Integer.valueOf(preferences.getString("offline_mode", "1"));
            offline_mode = OfflineMode.values()[offline];
        } else {
            offline_mode = OfflineMode.DISABLED;
        }
    }


    @Override
    public BaseSource[] getSources() {
        return new BaseSource[] {
                new AnnounceContentSource(this),
                new AnnounceSource(this),
                new Authenticate(this),
                new ChangeCourseSource(this),
                new ClassmateSource(this),
                new CourseListSource(this),
                new FileGroupSource(this),
                new FileSource(this),
                new FileGroupSource(this),
                new FileGroupFilesSource(this),
                new HomeworkContentSource(this),
                new HomeworkSource(this),
                new ScoreSource(this),
                new RollCallSource(this),
                new CustomCourseListSource(this),

                new DatabaseAnnounceSource(this),
                new DatabaseCourseListSource(this),
                new DatabaseScoreSource(this),
        };
    }

    @Override
    protected boolean filterSource(BaseSource source) {
        return offline_mode.compareTo(OfflineMode.DISABLED) != 0 || !source.property.isOffline;
    }

    public OfflineMode getOfflineMode() {
        return offline_mode;
    }

    public UserManager getUserManager() {
        return this.userManager;
    }

    public String getUsername() {
        return this.userManager.getUserName();
    }

    public String getPassword() {
        return this.userManager.getPassword();
    }

}

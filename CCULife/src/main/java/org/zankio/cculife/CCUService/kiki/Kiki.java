package org.zankio.cculife.CCUService.kiki;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.authentication.QueryStringAuth;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.kiki.source.local.DatabaseTimeTableSource;
import org.zankio.cculife.CCUService.kiki.source.remote.Authenticate;
import org.zankio.cculife.CCUService.kiki.source.remote.CourseListSource;
import org.zankio.cculife.CCUService.kiki.source.remote.TimetableSource;
import org.zankio.cculife.UserManager;

public class Kiki extends BaseRepo<String> {
    public enum OfflineMode {
        ALL,
        CLASS,
        BROWSERED,
        DISABLED
    }
    private OfflineMode offline_mode;
    private UserManager userManager;

    public Kiki(Context context) {
        super(context);
        setSession(new BaseSession<String>(new QueryStringAuth()));
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
    protected boolean filterSource(BaseSource source) {
        return offline_mode.compareTo(OfflineMode.DISABLED) != 0 || !source.property.isOffline;

    }

    @Override
    protected BaseSource[] getSources() {
        return new BaseSource[]{
                new CourseListSource(this),
                new TimetableSource(this),
                new Authenticate(this),

                new DatabaseTimeTableSource(this),
        };
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

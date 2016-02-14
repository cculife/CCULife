package org.zankio.cculife.CCUService.sourcequery;


import android.content.Context;

import org.jsoup.nodes.Document;
import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.sourcequery.source.remote.Authenticate;
import org.zankio.cculife.CCUService.sourcequery.source.remote.GradesInquiriesSource;
import org.zankio.cculife.UserManager;

public class ScoreQueryNew extends BaseRepo<Document> {
    private UserManager userManager;

    public ScoreQueryNew(Context context) {
        super(context);
        loadPreferences();
        setSession(new BaseSession<Document>());
    }

    private void loadPreferences() {
        this.userManager = UserManager.getInstance(getContext());
    }

    @Override
    protected BaseSource[] getSources() {
        return new BaseSource[]{
                new Authenticate(this),
                new GradesInquiriesSource(this),
        };
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

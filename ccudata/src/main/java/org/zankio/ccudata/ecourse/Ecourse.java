package org.zankio.ccudata.ecourse;

import android.content.Context;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.model.OfflineMode;
import org.zankio.ccudata.base.model.User;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.ecourse.source.local.DatabaseAnnounceSource;
import org.zankio.ccudata.ecourse.source.local.DatabaseCourseListSource;
import org.zankio.ccudata.ecourse.source.local.DatabaseScoreSource;
import org.zankio.ccudata.ecourse.source.remote.AnnounceContentSource;
import org.zankio.ccudata.ecourse.source.remote.AnnounceSource;
import org.zankio.ccudata.ecourse.source.remote.Authenticate;
import org.zankio.ccudata.ecourse.source.remote.ChangeCourseSource;
import org.zankio.ccudata.ecourse.source.remote.ClassmateSource;
import org.zankio.ccudata.ecourse.source.remote.CourseListSource;
import org.zankio.ccudata.ecourse.source.remote.CustomCourseListSource;
import org.zankio.ccudata.ecourse.source.remote.FileGroupFilesSource;
import org.zankio.ccudata.ecourse.source.remote.FileGroupSource;
import org.zankio.ccudata.ecourse.source.remote.HomeworkContentSource;
import org.zankio.ccudata.ecourse.source.remote.HomeworkSource;
import org.zankio.ccudata.ecourse.source.remote.RollCallSource;
import org.zankio.ccudata.ecourse.source.remote.ScoreSource;

public class Ecourse extends Repository {
    private OfflineMode offlineMode = OfflineMode.ALL;

    private User user = new User(this);
    public Ecourse(Context context) {
        super(context);
    }

    @Override
    protected <TData, TArgument> RequestTransformer<TData, TArgument> filterSource() {
        // no disabled
        if (getOfflineMode().compareTo(OfflineMode.DISABLED) != 0)
            return requestObservable -> requestObservable;

        // disabled offline
        return requestObservable ->
                requestObservable.filter(request -> !request.source().isOffline());
    }

    @Override
    protected BaseSource[] getSources() {
        return new BaseSource[] {
                new Authenticate(),
                new ChangeCourseSource(),

                // course list
                new CourseListSource(),
                new CustomCourseListSource(),

                // announce
                new AnnounceSource(),
                new AnnounceContentSource(),

                // classmate
                new ClassmateSource(),

                // file
                new FileGroupSource(),
                new FileGroupFilesSource(),

                // homework
                new HomeworkSource(),
                new HomeworkContentSource(),

                // rollcalls
                new RollCallSource(),

                // score
                new ScoreSource(),

                // offline
                new DatabaseCourseListSource(this),
                new DatabaseAnnounceSource(this),
                new DatabaseScoreSource(this),
        };
    }

    public User user(){
        return user;
    }
    public OfflineMode getOfflineMode() {
        return offlineMode;
    }

    public Ecourse setOfflineMode(OfflineMode offlineMode) {
        this.offlineMode = offlineMode;
        return this;
    }
}

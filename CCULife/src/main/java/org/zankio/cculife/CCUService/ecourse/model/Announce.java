package org.zankio.cculife.CCUService.ecourse.model;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.listener.OnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.source.remote.AnnounceContentSource;

public class Announce {
    public String url;
    public String date;
    public String title;
    public String content = null;
    public String important;
    public int browseCount;
    public boolean isnew;
    protected Ecourse ecourse;
    protected Course course;

    public Announce(Ecourse ecourse, Course course) {
        this.ecourse = ecourse;
        this.course = course;
    }

    public String getCourseID() {
        return this.course.courseid;
    }

    public Course getCourse() {
        return this.course;
    }

    public String getContent() {
        if (this.content != null) return this.content;

        try {
            ecourse.fetchSync(AnnounceContentSource.TYPE, this);

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return this.content;
    }

    //Todo: cancal task;
    public void getContent(final IOnUpdateListener listener) {
        if (this.content != null) {
            listener.onNext(AnnounceContentSource.TYPE, this, null);
            return;
        }

        IOnUpdateListener cacheListener = new OnUpdateListener() {
            @Override
            public void onNext(String type, Object data, BaseSource source) {
                Announce.this.content = (String) data;
                super.onNext(type, data, source);
            }
        };
        ecourse.fetch(AnnounceContentSource.TYPE, cacheListener, this);
    }
}

package org.zankio.ccudata.ecourse.model;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.source.remote.AnnounceContentSource;

import rx.Observable;

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
            ecourse.fetch(AnnounceContentSource.request(course, this))
                   .toBlocking()
                   .last();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return this.content;
    }

    //Todo: cancal task;
    public Observable<Response<Announce, AnnounceData>> getContent(boolean async) {
        if (this.content != null) {
            return Observable.just(new Response<>(this, null));
        }

        return ecourse.fetch(AnnounceContentSource.request(course, this));
    }
}

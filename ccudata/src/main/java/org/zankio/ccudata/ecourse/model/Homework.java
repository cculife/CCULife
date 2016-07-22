package org.zankio.ccudata.ecourse.model;


import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.source.remote.HomeworkContentSource;

import rx.Observable;

public class Homework {
    public int id;
    public String title;
    public String deadline;
    public String content;
    public String contentUrl;
    public String score;
    private Ecourse ecourse;
    private Course course;

    public Homework(Ecourse ecourse, Course course) {
        this.ecourse = ecourse;
        this.course = course;
    }

    public int getContentType() {
        if (this.contentUrl != null) return 1;
        else if (this.content != null) return 0;
        return -1;
    }

    public Observable<Response<Homework, HomeworkData>> getContent() {
        if (this.content != null) {
            return Observable.just(new Response<>(this, null));
        }

        return ecourse.fetch(HomeworkContentSource.request(course, this));
    }
}

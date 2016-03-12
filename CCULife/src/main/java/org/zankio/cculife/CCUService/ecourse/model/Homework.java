package org.zankio.cculife.CCUService.ecourse.model;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.source.remote.HomeworkContentSource;

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

    public void getContent(IOnUpdateListener<Homework> listener) {
        if (this.content != null) {
            listener.onNext(HomeworkContentSource.TYPE, this, null);
            return;
        }

        HomeworkContentSource.fetch(ecourse, listener, course, this);
    }
}

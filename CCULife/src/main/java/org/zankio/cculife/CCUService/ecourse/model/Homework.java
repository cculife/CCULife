package org.zankio.cculife.CCUService.ecourse.model;

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

    public String getContent() {
        try {
            return (String) ecourse.fetchSync(HomeworkContentSource.TYPE, course, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

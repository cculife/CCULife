package org.zankio.ccudata.ecourse.model;

public class AnnounceData extends CourseData {
    public Announce announce;

    public AnnounceData(Announce announce, Course course) {
        super(course);
        this.announce = announce;
    }
}

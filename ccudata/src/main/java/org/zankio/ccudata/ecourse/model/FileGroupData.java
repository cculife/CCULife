package org.zankio.ccudata.ecourse.model;

public class FileGroupData extends CourseData{
    public String href;

    public FileGroupData(String href, Course course) {
        super(course);

        this.href = href;
    }
}

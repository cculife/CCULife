package org.zankio.ccudata.ecourse.model;

public class HomeworkData extends CourseData {
    public Homework homework;

    public HomeworkData(Homework homework, Course course) {
        super(course);
        this.homework = homework;
    }
}

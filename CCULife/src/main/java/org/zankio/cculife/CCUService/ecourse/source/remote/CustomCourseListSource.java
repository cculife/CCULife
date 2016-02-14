package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.kiki.Kiki;
import org.zankio.cculife.CCUService.kiki.source.remote.CourseListSource;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;

public class CustomCourseListSource extends BaseSource<Course[]> {
    public final static String TYPE = "CUSTOM_COURSE_LIST";
    public final static String[] DATA_TYPES = { TYPE };
    public final static SourceProperty property;
    static  {
        property = new SourceProperty(
                SourceProperty.Level.MIDDLE,
                SourceProperty.Level.HIGH,
                false,
                DATA_TYPES
        );
    }

    public CustomCourseListSource(Ecourse context) {
        super(context, property);
    }

    @Override
    public Course[] fetch(String type, Object... arg) throws Exception {
        if (arg.length < 3)
            throw new Exception("arg is miss");

        Ecourse ecourse = (Ecourse) this.context;

        Kiki kiki = (Kiki) arg[2];
        int year = (int) arg[0],
            term = (int) arg[1];

        org.zankio.cculife.CCUService.kiki.model.Course[] courses;
        Course[] result;

        try {
            courses = (org.zankio.cculife.CCUService.kiki.model.Course[]) kiki.fetchSync(CourseListSource.TYPE, year, term);
            result = new Course[courses.length];

            for (int i = 0; i < courses.length; i++) {
                result[i] = new Course(ecourse);
                result[i].courseid = courses[i].getEcourseID();
                result[i].name = courses[i].Name;
                result[i].id = "";
                result[i].teacher = courses[i].Teacher;
                result[i].notice = 0;
                result[i].homework = 0;
                result[i].exam = 0;
                result[i].warning = false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

        return result;
    }
}

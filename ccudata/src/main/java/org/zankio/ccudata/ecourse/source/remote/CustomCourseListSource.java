package org.zankio.ccudata.ecourse.source.remote;

import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.KikiSemesterData;
import org.zankio.ccudata.kiki.Kiki;

import static org.zankio.ccudata.kiki.source.remote.CourseListSource.request;

@DataType(CustomCourseListSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@Deprecated
public class CustomCourseListSource extends BaseSource<KikiSemesterData, Course[]> {
    public final static String TYPE = "CUSTOM_COURSE_LIST";

    @Override
    public Course[] fetch(Request<Course[], KikiSemesterData> request) throws Exception {

        Ecourse ecourse = (Ecourse) getContext();

        Kiki kiki = request.args.kiki;
        int year = request.args.year;
        int term = request.args.term;

        org.zankio.ccudata.kiki.model.Course[] courses;
        Course[] result;

        courses = kiki.fetch(request(year, term))
                .toBlocking()
                .single()
                .data();

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

        return result;
    }

}

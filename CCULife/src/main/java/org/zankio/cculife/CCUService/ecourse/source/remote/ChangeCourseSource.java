package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.Connection;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.Course;

public class ChangeCourseSource extends BaseSource<Boolean> {
    public final static String TYPE = "CHANG_COURSE";
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

    public ChangeCourseSource(Ecourse context) {
        super(context, property);
    }

    @Override
    public Boolean fetch(String type, Object... arg) throws Exception {
        if (arg.length < 1) throw new Exception("arg is miss");

        Ecourse context = (Ecourse) this.context;
        BaseSession<String> session = context.getSession();
        Course course = (Course) arg[0];
        if (session == null) throw new Exception("Session is miss");

        CourseSource.authenticate(context, session);

        Connection connection;

        connection = context.buildConnection(String.format(Url.COURSE_SELECT, course.courseid));
        connection.get();
        session.setIdentity(course.courseid);
        return true;
    }
}

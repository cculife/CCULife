package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;

public class CourseListSource extends BaseSource<Course[]> {
    public final static String TYPE = "COURSE_LIST";
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

    public CourseListSource(Ecourse context) {
        super(context, property);
    }

    public Course[] parseCourses(Ecourse ecourse, Document document) {
        Elements tables, courses = null, fields;
        Course[] result;
        tables = document.select("table");

        for (Element table : tables) {
            courses = table.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");
            if (courses.size() > 0) break;
        }
        if (courses == null) return null;
        result = new Course[courses.size()];

        for (int i = 0; i < courses.size(); i++) {
            fields = courses.get(i).getElementsByTag("td");
            result[i] = new Course(ecourse);

            result[i].courseid = fields.get(3).child(0).child(0).attr("href").replace("../login_s.php?courseid=", "");
            result[i].id = fields.get(2).text();
            result[i].name = fields.get(3).text();
            result[i].teacher = fields.get(4).text();
            result[i].notice = Integer.parseInt(fields.get(5).text());
            result[i].homework = Integer.parseInt(fields.get(6).text());
            result[i].exam = Integer.parseInt(fields.get(7).text());
            result[i].warning = !fields.get(9).text().equals("--");

        }

        return result;
    }

    @Override
    public Course[] fetch(String type, Object... arg) throws Exception {
        Ecourse context = (Ecourse) this.context;
        BaseSession session = context.getSession();
        if (session == null) throw new Exception("Session is miss");

        CourseSource.authenticate(context, session);

        try {
            Connection connection;

            connection = context.buildConnection(Url.COURSE_LIST);

            return parseCourses(context, connection.get());

        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }
    }
}

package org.zankio.ccudata.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.annotation.Charset;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.base.source.http.annotation.Url;
import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@Url(Urls.COURSE_LIST)
@Charset("big5")

@DataType(CourseListSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
public class CourseListSource extends EcourseSource<CourseData, Course[]> {
    public final static String TYPE = "COURSE_LIST";

    public static Request<Course[], CourseData> request() {
        return new Request<>(TYPE, null, Course[].class);
    }

    @Override
    protected Course[] parse(Request<Course[], CourseData> request, HttpResponse response, Document document) throws Exception {
        Ecourse ecourse = (Ecourse) getContext();
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
}

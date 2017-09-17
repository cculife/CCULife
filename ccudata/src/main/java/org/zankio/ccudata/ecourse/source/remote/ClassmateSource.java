package org.zankio.ccudata.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.base.source.http.annotation.Url;
import org.zankio.ccudata.ecourse.annotation.ChangeCourse;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Classmate;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@Url(Urls.COURSE_CLASSMATE)

@DataType(ClassmateSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)

@ChangeCourse
@Deprecated
public class ClassmateSource extends EcourseSource<CourseData, Classmate[]> {
    public final static String TYPE = "CLASSMATE";

    public static Request<Classmate[], CourseData> request(Course course) {
        return new Request<>(TYPE, new CourseData(course), Classmate[].class);
    }

    @Override
    protected Classmate[] parse(Request<Classmate[], CourseData> request, HttpResponse response, Document document) throws Exception {
        Elements list, field;

        Classmate[] result;

        list = document.select("tr[bgcolor=#F0FFEE], tr[bgcolor=#E6FFFC]");

        result = new Classmate[list.size()];

        for (int i = 0; i < list.size(); i++) {
            field = list.get(i).select("td");

            result[i] = new Classmate();
            result[i].name = field.get(3).text();
            result[i].department = field.get(1).text();
            result[i].gender = field.get(5).text();
            result[i].studentId = field.get(2).text();
        }

        return result;
    }
}

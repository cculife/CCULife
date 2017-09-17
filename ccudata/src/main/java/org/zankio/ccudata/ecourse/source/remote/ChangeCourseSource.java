package org.zankio.ccudata.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")

@DataType(ChangeCourseSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
public class ChangeCourseSource extends EcourseSource<CourseData, Boolean> {
    public final static String TYPE = "CHANG_COURSE";

    @Override
    public void initHTTPRequest(Request<Boolean, CourseData> request) {
        super.initHTTPRequest(request);
        httpParameter(request)
                .url(String.format(Urls.COURSE_SELECT, getCourse(request).courseid));
    }

    @Override
    protected Boolean parse(Request<Boolean, CourseData> request, HttpResponse response, Document document) throws Exception {
        setSessionCourseID(context, getCourse(request).courseid);
        return true;
    }

    public static Request<Boolean, CourseData> request(Course course) {
        return new Request<>(TYPE, new CourseData(course), Boolean.class);
    }
}

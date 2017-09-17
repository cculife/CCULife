package org.zankio.ccudata.ecourse.source.remote;

import org.jsoup.nodes.Document;
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
import org.zankio.ccudata.ecourse.annotation.ChangeCourse;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.model.Homework;
import org.zankio.ccudata.ecourse.utils.ParseUtils;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@Url(Urls.COURSE_HOMEWORK)
@Charset("big5")

@DataType(HomeworkSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@ChangeCourse
public class HomeworkSource extends EcourseSource<CourseData, Homework[]> {
    public final static String TYPE = "HOMEWORK";

    public static Request<Homework[], CourseData> request(Course course) {
        return new Request<>(TYPE, new CourseData(course), Homework[].class);
    }

    @Override
    protected Homework[] parse(Request<Homework[], CourseData> request, HttpResponse response, Document document) throws Exception {
        Course course = request.args.course;
        Elements row, field;
        Homework[] result;

        row = ParseUtils.parseRow(document);
        result = new Homework[row.size()];

        for (int i = 0; i < result.length; ++i) {
            field = ParseUtils.parseField(row.get(i));
            result[i] = new Homework(course.getEcourse(), course);
            result[i].id = Integer.valueOf(field.get(5).child(1).val());
            result[i].title = field.get(1).text();
            result[i].deadline = field.get(3).text();
            result[i].score = field.get(4).text();
        }

        return result;
    }
}

package org.zankio.ccudata.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.constant.Exceptions;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.annotation.Charset;
import org.zankio.ccudata.base.source.http.annotation.FollowRedirect;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.ecourse.annotation.ChangeCourse;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.Homework;
import org.zankio.ccudata.ecourse.model.HomeworkData;

import java.util.Locale;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@FollowRedirect(false)
@Charset("big5")

@DataType(HomeworkContentSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@ChangeCourse
public class HomeworkContentSource extends EcourseSource<HomeworkData, Homework> {
    public final static String TYPE = "HOMEWORK_CONTENT";

    public void parseHomeworkContent(Document document, Homework homework) throws Exception {
        Elements content;

        content = document.select("pre");
        if (content.size() == 0) throw new Exception(Exceptions.PARSE_FAIL);

        homework.content = content.html();
    }

    public static Request<Homework, HomeworkData> request(Course course, Homework homework) {
        return new Request<>(TYPE, new HomeworkData(homework, course), Homework.class);
    }

    @Override
    public void initHTTPRequest(Request<Homework, HomeworkData> request) {
        super.initHTTPRequest(request);
        HomeworkData homeworkData = request.args;
        httpParameter(request)
                .url(String.format(Locale.US, Urls.COURSE_HOMEWORK_CONTENT, homeworkData.homework.id));
    }

    @Override
    protected Homework parse(
            Request<Homework, HomeworkData> request,
            HttpResponse response,
            Document document
    ) throws Exception {

        Homework homework = request.args.homework;
        String location = response.header("location");

        if (location != null) {
            if (!location.startsWith("http")) {
                homework.contentUrl = "https://ecourse.ccu.edu.tw/php/Testing_Assessment/" + location;
            } else {
                homework.contentUrl = location;
            }
        } else {
            parseHomeworkContent(document, homework);
        }
        return homework;
    }
}

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
import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.annotation.ChangeCourse;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Announce;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@Url(Urls.COURSE_ANNOUNCE)
@Charset("big5")

@DataType(AnnounceSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@ChangeCourse
public class AnnounceSource extends EcourseSource<CourseData, Announce[]> {
    public final static String TYPE = "ANNOUNCE";

    public static Request<Announce[],CourseData> request(Course course) {
        return new Request<>(TYPE, new CourseData(course), Announce[].class);
    }

    @Override
    protected Announce[] parse(Request<Announce[], CourseData> request, HttpResponse response, Document document) throws Exception {
        Course course = request.args.course;

        Elements announces, fields;
        Announce[] result;

        announces = document.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");
        result = new Announce[announces.size()];

        for(int i = 0; i < result.length; i++) {
            fields = announces.get(i).select("td");

            result[i] = new Announce((Ecourse) getContext(), course);
            result[i].date =  fields.get(0).text();
            result[i].title = fields.get(2).text();
            result[i].important = fields.get(1).text();
            result[i].browseCount = Integer.parseInt(fields.get(3).text());
            result[i].isnew = fields.get(2).select("img").size() > 0;
            result[i].url = fields.get(2).child(0).child(0).child(0).attr("onclick").split("'")[1].replace("./", "");
        }

        return result;
    }

}

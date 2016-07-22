package org.zankio.ccudata.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.annontation.Charset;
import org.zankio.ccudata.base.source.http.annontation.Method;
import org.zankio.ccudata.base.source.http.annontation.Url;
import org.zankio.ccudata.ecourse.annotation.ChangeCourse;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.model.RollCall;

@Method("GET")
@Url(Urls.COURSE_ROLLCALL)
@Charset("big5")

@DataType(RollCallSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@ChangeCourse
public class RollCallSource extends EcourseSource<CourseData, RollCall[]> {
    public final static String TYPE = "ROLL_CALL";

    public static Request<RollCall[], CourseData> request(Course course) {
        return new Request<>(TYPE, new CourseData(course), RollCall[].class);
    }

    @Override
    protected RollCall[] parse(Request<RollCall[], CourseData> request, HttpResponse response, Document document) throws Exception {
        Elements rollcalls, fields;
        RollCall[] result;

        rollcalls = document.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE], tr[bgcolor=#000066]:not(:first-child)");
        if (rollcalls.size() == 1) return new RollCall[0];

        result = new RollCall[rollcalls.size()];

        for(int i = 0; i < result.length; i++) {
            fields = rollcalls.get(i).select("td");

            result[i] = new RollCall();
            result[i].date =  fields.get(0).text();
            result[i].comment = fields.get(1).text();
        }

        return result;
    }
}

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
import org.zankio.ccudata.base.utils.DateUtils;
import org.zankio.ccudata.ecourse.annotation.ChangeCourse;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.model.RollCall;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@Url(Urls.COURSE_ROLLCALL)
@Charset("big5")

@DataType(RollCallSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@ChangeCourse
public class RollCallSource extends EcourseSource<CourseData, RollCall> {
    public final static String TYPE = "ROLL_CALL";

    public static Request<RollCall, CourseData> request(Course course) {
        return new Request<>(TYPE, new CourseData(course), RollCall.class);
    }

    @Override
    protected RollCall parse(Request<RollCall, CourseData> request, HttpResponse response, Document document) throws Exception {
        Elements rollcalls, fields;
        RollCall result = new RollCall();
        int i;

        rollcalls = document.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE], tr[bgcolor=#000066]:not(:first-child)");
        if (rollcalls.size() == 1) return result;

        result.records = new RollCall.Record[rollcalls.size() - 1];

        for(i = 0; i < result.records.length; i++) {
            fields = rollcalls.get(i).select("td");

            result.records[i] = result.new Record();
            result.records[i].date = DateUtils.normalizeDateString("yyyy-MM-dd", fields.get(0).text());
            result.records[i].comment = fields.get(1).text();

            if (result.records[i].comment.contains("缺席"))
                result.records[i].absent = true;
        }

        fields = rollcalls.get(i).select("td");
        Pattern patternAttend = Pattern.compile("Attendance:\\s*(\\d+)");
        Pattern patternAbsent = Pattern.compile("缺席:\\s*(\\d+)");

        String statistics = fields.get(1).text();
        Matcher matcher = patternAbsent.matcher(statistics);
        if (matcher.find()) {
            result.absent = Integer.valueOf(matcher.group(1));
        }

        matcher = patternAttend.matcher(statistics);
        if (matcher.find()) {
            result.attend = Integer.valueOf(matcher.group(1));
        }

        return result;
    }
}

package org.zankio.cculife.CCUService.ecourse.source.remote;

import android.os.AsyncTask;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.model.RollCall;

public class RollCallSource extends CourseSource<RollCall[]> {
    public final static String TYPE = "ROLL_CALL";
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

    public RollCallSource(Ecourse context) {
        super(context, property);
    }

    public static AsyncTask[] fetch(Ecourse context, IOnUpdateListener listener, Course course) {
        return context.fetch(TYPE, listener, course);
    }

    @Override
    protected String getUrl(Course course) { return Url.COURSE_ROLLCALL; }

    @Override
    public RollCall[] parse(Document document, Course course) {
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

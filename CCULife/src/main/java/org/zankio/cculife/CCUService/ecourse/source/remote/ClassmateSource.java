package org.zankio.cculife.CCUService.ecourse.source.remote;

import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.Classmate;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.Debug;

public class ClassmateSource extends CourseSource<Classmate[]> {
    public final static String TYPE = "CLASSMATE";
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

    public ClassmateSource(Ecourse context) {
        super(context, property);
    }

    @Override
    public Classmate[] parse(Document document, Course course) {
        if (Debug.log)
            Log.d(this.getClass().toString(), "parse");
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

    @Override
    protected String getUrl(Course course) {
        return Url.COURSE_CLASSMATE;
    }
}

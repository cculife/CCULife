package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.model.Homework;
import org.zankio.cculife.CCUService.ecourse.utils.ParseUtils;

public class HomeworkSource extends CourseSource<Homework[]> {
    public final static String TYPE = "HOMEWORK";
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

    public HomeworkSource(Ecourse context) { super(context, property); }

    public static void fetch(Ecourse context, IOnUpdateListener listener, Course course) {
        context.fetch(TYPE, listener, context);
    }

    @Override
    public Homework[] parse(Document document, Course course) {
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

    @Override
    protected String getUrl(Course course) {
        return Url.COURSE_HOMEWORK;
    }

}

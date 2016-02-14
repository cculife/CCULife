package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.Announce;
import org.zankio.cculife.CCUService.ecourse.model.Course;

public class AnnounceSource extends CourseSource<Announce[]> {
    public final static String TYPE = "ANNOUNCE";
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

    public AnnounceSource(Ecourse context) {
        super(context, property);
    }

    @Override
    protected String getUrl(Course course) { return Url.COURSE_ANNOUNCE; }

    @Override
    public Announce[] parse(Document document, Course course) {
        Elements announces, fields;
        Announce[] result;

        announces = document.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");
        result = new Announce[announces.size()];

        for(int i = 0; i < result.length; i++) {
            fields = announces.get(i).select("td");

            result[i] = new Announce((Ecourse) context, course);
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

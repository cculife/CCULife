package org.zankio.ccudata.kiki.source.remote;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.HTTPJsoupSource;
import org.zankio.ccudata.base.source.http.HTTPParameter;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.base.source.http.annotation.Url;

import java.util.Locale;

@Method("POST")
@Url("https://kiki.ccu.edu.tw/~ccmisp06/cgi-bin//Query/Query_by_time2.cgi")

@DataType(AllCourseSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.MIDDLE)
public class AllCourseSource extends HTTPJsoupSource<Void, String> {
    public static final String TYPE = "ALL_COURSE";
    private static final String[] WEEKS = new String[] { "1", "2", "3", "4", "5", "6", "7" };
    private static final String[] CLASS_TIMES = new String[] {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ,
            "11", "12", "13", "14", "15",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"
    };

    private static final String[] DEPTS = new String[] {
            "1014", "1016", "1104", "1106", "1154", "1156", "1204", "1206", "1254", "1256",
            "1306", "1316", "1326", "1366", "1416", "2014", "2016", "2104", "2106", "2156",
            "2204", "2206", "2316", "2354", "2386", "2406", "2456", "2504", "2556", "2604",
            "2606", "3014", "3016", "3104", "3106", "3154", "3156", "3204", "3206", "3304",
            "3306", "3354", "3356", "3416", "3656", "3706", "4014", "4016", "4104", "4106",
            "4154", "4156", "4204", "4206", "4254", "4256", "4304", "4306", "4416", "4456",
            "4458", "5014", "5016", "5104", "5106", "5154", "5156", "5204", "5206", "5264",
            "5266", "5304", "5306", "5356", "5456", "5556", "6014", "6016", "6054", "6056",
            "6104", "6204", "6304", "6306", "7014", "7016", "7104", "7106", "7156", "7254",
            "7256", "7306", "7356", "7364", "7406", "7456", "7506", "F000", "I001", "V000",
            "Z121"
    };

    public static Request<String, Void> request() {
        return new Request<>(TYPE, null, String.class);
    }

    @Override
    public void initHTTPRequest(Request<String, Void> request) {
        super.initHTTPRequest(request);
        HTTPParameter parameter = httpParameter(request);
        for (String week : WEEKS) {
            for (String classTime : CLASS_TIMES) {
                parameter.fields(String.format(Locale.US, "%s_%s", week, classTime), "999");
            }
        }

        for (String dept : DEPTS) {
            parameter.fields("dept_multi", dept);
        }
    }

    @Override
    protected String parse(Request<String, Void> request, HttpResponse response, Document document) throws Exception {
        StringBuilder sb = new StringBuilder();
        Element table = document.select("table").get(1);
        Elements rows = table.select("tr:not(:nth-of-type(1))");
        for (Element row : rows) {
            Elements fields = row.select("td");

            String dept = fields.get(0).text();
            String grade = fields.get(1).text();
            String courseID = fields.get(2).text();
            String classID = fields.get(3).text();
            String name = fields.get(4).text();
            String hour = fields.get(5).text();
            String time = fields.get(6).text();
            String teacher = fields.get(7).text();
            String room = fields.get(8).text();
            String limit = fields.get(9).text();
            String comment = fields.get(10).text();

            sb.append(dept).append("\t")
                    .append(grade).append("\t")
                    .append(courseID).append("\t")
                    .append(classID).append("\t")
                    .append(name).append("\t")
                    .append(hour).append("\t")
                    .append(time).append("\t")
                    .append(teacher).append("\t")
                    .append(room).append("\t")
                    .append(limit).append("\t")
                    .append(comment).append("\n");
        }

        return sb.toString();
    }
}

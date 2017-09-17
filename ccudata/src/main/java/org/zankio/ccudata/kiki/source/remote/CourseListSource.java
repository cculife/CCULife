package org.zankio.ccudata.kiki.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.HTTPParameter;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.base.source.http.annotation.Url;
import org.zankio.ccudata.kiki.model.Course;
import org.zankio.ccudata.kiki.model.SemesterData;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@Url("https://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/class_new/Selected_View00.cgi")

@DataType(CourseListSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
public class CourseListSource extends KikiSource<SemesterData, Course[]> {
    public final static String TYPE = "COURSE_LIST";
    private static final int SELECTED_CLASS_NAME = 0;
    private static final int SELECTED_CLASS_TIME = 1;
    private static final int SELECTED_CLASS_ROOM = 2;
    private static final int SELECTED_CLASS_TEACHER = 3;
    private static final int SELECTED_CLASS_COURSEID = 4;
    private static final int SELECTED_CLASS_ID = 5;
    private static final int SELECTED_CLASS_CREDIT = 6;
    private static final int SELECTED_CLASS_CREDITTYPE = 7;
    private static final int SELECTED_CLASS_OUTLINELINK = 8;
    private static final int SELECTED_FIELD_LENGTH = 9;

    public static Request<Course[], SemesterData> request() {
        return request(-1, -1);
    }

    public static Request<Course[], SemesterData> request(int year, int term) {
        return new Request<>(TYPE, new SemesterData(year, term), Course[].class);
    }

    @Override
    public void initHTTPRequest(Request<Course[], SemesterData> request) {
        super.initHTTPRequest(request);
        SemesterData semesterData = request.args;
        HTTPParameter parameter = httpParameter(request);

        if (semesterData.year > 0 && semesterData.term > 0) {
            parameter.queryStrings("year", String.valueOf(semesterData.year));
            parameter.queryStrings("term", String.valueOf(semesterData.term));
        }

    }

    @Override
    protected Course[] parse(Request<Course[], SemesterData> request, HttpResponse response, Document document) throws Exception {
        int year = request.args.year;
        int term = request.args.term;

        Elements classes, fields, anchor, table;
        String fieldText
                , Contents[] = new String[SELECTED_FIELD_LENGTH]
                , Headers[] = new String[SELECTED_FIELD_LENGTH];

        Course[] result;
        Course mCourse;

        int classIndex[] = new int[SELECTED_FIELD_LENGTH];

        Headers[SELECTED_CLASS_NAME] = "科目名稱";
        Headers[SELECTED_CLASS_TIME] = "星期節次";
        Headers[SELECTED_CLASS_ROOM] = "教室";
        Headers[SELECTED_CLASS_TEACHER] = "授課教師";
        Headers[SELECTED_CLASS_COURSEID] = "科目代碼";
        Headers[SELECTED_CLASS_ID] = "班別";
        Headers[SELECTED_CLASS_CREDIT] = "學分";
        Headers[SELECTED_CLASS_CREDITTYPE] = "學分歸屬";
        Headers[SELECTED_CLASS_OUTLINELINK] = "大綱";

        for (int i = 0; i < SELECTED_FIELD_LENGTH; i++) classIndex[i] = -1;
        table = document.select("table");
        if (table.size() < 2) return null;

        classes = table.get(1).select("tr");

        if (classes.size() == 0) return null;

        //去掉 th
        result = new Course[classes.size() - 1];

        // 取得正確的欄位索引值
        fields = classes.get(0).select("th");
        for (int i = 0; i < fields.size(); i++) {
            fieldText = fields.get(i).text();

            for (int j = 0; j < SELECTED_FIELD_LENGTH; j++) {
                if (classIndex[j] == -1 && Headers[j].equals(fieldText)) {
                    classIndex[j] = i;
                    break;
                }
            }

        }

        for (int i = 1; i < classes.size(); i++) {

            fields = classes.get(i).select("th");

            for (int j = 0; j < SELECTED_FIELD_LENGTH; j++) {
                if (classIndex[j] >= 0) {
                    if (j == SELECTED_CLASS_OUTLINELINK) {
                        anchor = fields.get(classIndex[j]).select("a");
                        if (anchor.size() > 0) {
                            Contents[j] = anchor.get(0).attr("href");
                        }
                    } else {
                        Contents[j] = fields.get(classIndex[j]).text();
                    }
                } else {
                    Contents[j] = "";
                }
            }

            mCourse = new Course();

            mCourse.Name = Contents[SELECTED_CLASS_NAME];
            mCourse.ClassRoom = Contents[SELECTED_CLASS_ROOM];
            mCourse.Teacher = Contents[SELECTED_CLASS_TEACHER];
            //mCourse.Time = Contents[SELECTED_CLASS_TIME];
            mCourse.CreditType = Contents[SELECTED_CLASS_CREDITTYPE];
            mCourse.OutlineLink = Contents[SELECTED_CLASS_OUTLINELINK];
            mCourse.ClassID = Contents[SELECTED_CLASS_ID];
            mCourse.CourseID = Contents[SELECTED_CLASS_COURSEID];
            //mCourse.credit = Integer.parseInt(Contents[SELECTED_CLASS_CREDIT]);
            mCourse.term = term;
            mCourse.year = year;

            result[i - 1] = mCourse;
        }

        return result;
    }
}

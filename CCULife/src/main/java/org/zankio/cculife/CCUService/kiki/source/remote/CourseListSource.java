package org.zankio.cculife.CCUService.kiki.source.remote;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.kiki.Kiki;
import org.zankio.cculife.CCUService.kiki.model.Course;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;

public class CourseListSource extends BaseSource<Course[]> {
    public final static String TYPE = "COURSE_LIST";
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

    public CourseListSource(Kiki context) {
        super(context, property);
    }

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

    public Course[] parseCourseList(int year, int term, Document document) {
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

    public static void fetch(Kiki context, IOnUpdateListener<Course[]> listener) {
        context.fetch(CourseListSource.TYPE, listener);
    }

    public static void fetch(Kiki context, int year, int term, IOnUpdateListener<Course[]> listener) {
        context.fetch(CourseListSource.TYPE, listener, year, term);
    }

    public Course[] getCourseList(int year, int term) throws Exception {
        Kiki context = (Kiki) this.context;
        BaseSession<String> session = context.getSession();
        if (session == null) throw new Exception("Session is miss");

        Connection connection;
        String url;

        if (!session.isAuthenticated())
            context.fetchSync(Authenticate.TYPE, context.getUsername(), context.getPassword());

        try {
            url = "https://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/class_new/Selected_View00.cgi?"+
                    (year > 0 && term > 0 ? ("year=" + year + "&term=" + term) : "");

            connection = context.buildConnection(url);

            return parseCourseList(year, term, connection.get());

        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

    }

    @Override
    public Course[] fetch(String type, Object... arg) throws Exception {
        int year, term;
        if (arg.length < 2) {
            year = -1;
            term = -1;
        } else {
            year = (int) arg[0];
            term = (int) arg[1];
        }

        return getCourseList(year, term);
    }
}

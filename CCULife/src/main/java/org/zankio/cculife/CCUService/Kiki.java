package org.zankio.cculife.CCUService;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.Net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;

import static org.zankio.cculife.ui.CourseSchedule.TimeTableWeekPage.randomColor;

public class Kiki extends BaseService{

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

    private Context context;

    public Kiki(Context context) {
        this.context = context;
    }

    @Override
    public boolean getSession() throws Exception {
        SessionManager sessionManager = SessionManager.getInstance(context);

        if (!sessionManager.isLogined()) throw Exceptions.getNeedLoginException();

        Connection connection;
        Document document;
        Elements logout;
        connection = Jsoup.connect("http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/class_new/bookmark.php")
                          .timeout(Net.CONNECT_TIMEOUT);
        connection.data("id", sessionManager.getUserName())
                .data("password", sessionManager.getPassword())
                .data("term", "on");
        try {

            document = connection.post();
            logout = document.select("a[href^=logout.php?session_id=]");
            if (logout.size() == 0) return false;

            SESSIONID = logout.attr("href").replace("logout.php?session_id=", "");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

    }

    public TimeTable getTimeTable() throws Exception {
        if (SESSIONID == null) return null;

        Connection connection;
        Document document;
        Elements classes, fields, table;
        String fieldText, className = "", classTime = "", classRoom = "", classTeacher = "";

        String weekName = "日一二三四五六";
        String[] daylist;
        String[] timelist;

        TimeTable result;
        TimeTable.Class mClass;

        String classNameHeader = "科目名稱"
               , classTimeHeader = "星期節次"
               , classRoomHeader = "教室"
               , classTeacherHeader = "授課教師";

        int classNameIndex = -1
          , classTimeIndex = -1
          , classRoomIndex = -1
          , classTeacherIndex = -1;

        int classColor;

        connection = Jsoup.connect("http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/class_new/Selected_View00.cgi?session_id=" + SESSIONID)
                          .timeout(Net.CONNECT_TIMEOUT);

        try {

            document = connection.get();
            //TODO only use select
            table = document.select("table");
            if (table.size() < 2) return null;

            classes = table.get(1).select("tr");
            result = new TimeTable();

            if (classes.size() == 0) return null;

            // 取得正確的欄位索引值
            fields = classes.get(0).select("th");
            for (int i = 0; i < fields.size(); i++) {
                fieldText = fields.get(i).text();
                if (classNameHeader.equals(fieldText))
                    classNameIndex = i;
                else if (classRoomHeader.equals(fieldText))
                    classRoomIndex = i;
                else if (classTeacherHeader.equals(fieldText))
                    classTeacherIndex = i;
                else if (classTimeHeader.equals(fieldText))
                    classTimeIndex = i;
            }

            for (int i = 1; i < classes.size(); i++) {

                fields = classes.get(i).select("th");

                if (classNameIndex >= 0) className = fields.get(classNameIndex).text();
                if (classRoomIndex >= 0) classRoom = fields.get(classRoomIndex).text();
                if (classTeacherIndex >= 0) classTeacher = fields.get(classTeacherIndex).text();
                if (classTimeIndex >= 0) classTime = fields.get(classTimeIndex).text();
                classColor = randomColor();

                int k = 0;
                int processingTime = -1;
                classTime = classTime.replaceAll(" ", "");
                daylist = classTime.split("[日一二三四五六]");
                for (int j = 1; j < daylist.length; j++) {

                    timelist = daylist[j].split(",");

                    for (; k < classTime.length(); k++) {
                        if ((processingTime = weekName.indexOf(classTime.substring(k, k + 1))) > -1) {
                            k++;
                            break;
                        }
                    }

                    if (processingTime == -1) continue;
                    mClass = result.new Class();
                    mClass.name = className;
                    mClass.classroom = classRoom;
                    mClass.color = classColor;
                    mClass.teacher = classTeacher;
                    parseClassTime(mClass, processingTime, timelist[0]);
                    for (int l = 1; l < timelist.length; l++) {
                        mergeClassTime(mClass, processingTime, timelist[l]);
                    }
                    result.days[processingTime].classList.add(mClass);
                }
            }
            result.sort();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

    }

    private void mergeClassTime(TimeTable.Class mClass, int dayofweek, String time){
        int endHour, endMinute;

        int origTime;
        int[] hourData = {7, 8, 10, 11, 13, 14, 16, 17, 19, 20};

        if (time == null) return;

        if (time.matches("[ABCDEFGHIJ]")) {

            origTime = "ABCDEFGHIJ".indexOf(time);

            if (origTime % 2 == 0) {
                endHour = hourData[origTime] + 1;
                endMinute = 30;
            } else {
                endHour = hourData[origTime] + 2;
                endMinute = 0;
            }

        } else {

            origTime = Integer.parseInt(time);
            endHour = origTime + 7;
            endMinute = 0;

        }
        mClass.end.set(Calendar.HOUR, endHour);
        mClass.end.set(Calendar.MINUTE, endMinute);
        mClass.end.set(Calendar.DAY_OF_WEEK, dayofweek);
    }

    private void parseClassTime(TimeTable.Class mClass, int dayofweek, String time) {
        int startHour, startMinute;
        int endHour, endMinute;

        int origTime;
        int[] hourData = {7, 8, 10, 11, 13, 14, 16, 17, 19, 20};

        if (time == null) return;

        if (time.matches("[ABCDEFGHIJ]")) {

            origTime = "ABCDEFGHIJ".indexOf(time);
            startHour = hourData[origTime];

            if (origTime % 2 == 0) {
                endHour = startHour + 1;
                startMinute = 15;
                endMinute = 30;
            } else {
                endHour = startHour + 2;
                startMinute = 45;
                endMinute = 0;
            }

        } else {

            origTime = Integer.parseInt(time);
            startHour = origTime + 6;
            endHour = startHour + 1;
            startMinute = 10;
            endMinute = 0;

        }

        mClass.start = new GregorianCalendar(0, 0, 0);
        mClass.start.set(Calendar.HOUR, startHour);
        mClass.start.set(Calendar.MINUTE, startMinute);
        mClass.start.set(Calendar.DAY_OF_WEEK, dayofweek);

        mClass.end = new GregorianCalendar(0, 0, 0);
        mClass.end.set(Calendar.HOUR, endHour);
        mClass.end.set(Calendar.MINUTE, endMinute);
        mClass.end.set(Calendar.DAY_OF_WEEK, dayofweek);

    }

    //ToDo Day-Class to Class-Day

    public class TimeTable {

        public Day[] days;

        public TimeTable() {
            days = new Day[7];
            for (int i = 0; i < 7; i++) {
                days[i] = new Day();
            }
        }

        public class Day {
            public ArrayList<Class> classList;

            public Day() {
                classList = new ArrayList<Class>();
            }
        }

        public class Class {
            public String name;
            public String classroom;
            public String teacher;
            public Calendar start;
            public Calendar end;
            public int color;
        }

        public void sort() {
            Comparator<Class> comparator = new Comparator<Class>() {
                public int compare(Class a, Class b) {
                    return a.start.compareTo(b.start);
                }
            };

            for (int i = 0; i < 7; i++) {
                Collections.sort(days[i].classList, comparator);
            }
        }
    }

    public Ecourse getEcourseCourse(int year, int term, Ecourse ecourse) throws Exception {
        Course[] courses = getCourseList(year, term);

        return null;
    }

    public static class Course {
        public String CourseID;
        public String ClassID;
        public String Name;
        public String Teacher;
        public int Credit;
        public String CreditType;
        public Time[] Time;
        public String ClassRoom;
        public String OutlineLink;
        public int term;
        public int year;

        public String getEcourseID() {
            if ("".equals(OutlineLink)) return null;
            if (year > 0 && term > 0)
                OutlineLink = OutlineLink
                        .replaceAll("([\\?&])year=\\d+", "$1year=" + year)
                        .replaceAll("([\\?&])term=\\d+", "$1term=" + term);

            Connection connection;
            String location, result;

            connection = Jsoup.connect(OutlineLink).timeout(Net.CONNECT_TIMEOUT);
            connection.followRedirects(false);


            try {
                location = connection.execute().header("Location");
                if (location != null) {
                    result = Uri.parse(location).getQueryParameter("courseid");
                    if (result != null)
                        return String.format("%d_%d_%s", year, term, result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        public static class Time {
            public static Time prase(String data) {
                return null;
            }
        }
    }
    public Course[] getCourseList() throws Exception {
        return getCourseList(-1, -1);
    }
    public Course[] getCourseList(int year, int term) throws Exception {
        if (SESSIONID == null) return null;

        Connection connection;
        Document document;
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

        connection = Jsoup.connect("http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/class_new/Selected_View00.cgi?"+
                (year > 0 && term > 0 ? ("year=" + year +"&term=" + term + "&") : "") + "session_id=" + SESSIONID)
                .timeout(Net.CONNECT_TIMEOUT);

        Log.e("", "URL : http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/class_new/Selected_View00.cgi?"+
                (year > 0 && term > 0 ? ("year=" + year +"&term=" + term + "&") : "") + "session_id=" + SESSIONID);

        try {

            document = connection.get();
            table = document.select("table");
            if (table.size() < 2) return null;

            classes = table.get(1).select("tr");
            Log.e("", "html : " + document.html());

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
                        Log.e("", "classIndex[" + j +"] = " + i);
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
                //mCourse.Credit = Integer.parseInt(Contents[SELECTED_CLASS_CREDIT]);
                mCourse.term = term;
                mCourse.year = year;

                Log.e("", mCourse.Name + "\n" +
                        mCourse.ClassRoom + "\n" +
                        mCourse.Teacher + "\n" +
                        mCourse.CreditType + "\n" +
                        mCourse.OutlineLink + "\n" +
                        mCourse.ClassID + "\n" +
                        mCourse.CourseID + "\n");
                result[i - 1] = mCourse;
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

    }
}

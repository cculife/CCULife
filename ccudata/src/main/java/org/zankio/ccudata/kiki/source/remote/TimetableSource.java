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
import org.zankio.ccudata.kiki.model.SemesterData;
import org.zankio.ccudata.kiki.model.TimeTable;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@Url("https://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/class_new/Selected_View00.cgi")

@DataType(TimetableSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)

public class TimetableSource extends KikiSource<SemesterData, TimeTable> {
    private static int[] colors = {0x3333B5E5, 0x33AA66CC, 0x3399CC00, 0x33FFBB33, 0x33FF4444};
    public final static String TYPE = "TIMETABLE";

    public static void parseClassTime(TimeTable timeTable, TimeTable.Class mClass, String classTime) {
        /*Format example:
            一G 三G
            四12,13,14
            五C,D
        */
        String weekName = "日一二三四五六";
        String[] daylist;
        String[] timelist;

        int k = 0, m;
        int processingTime = -1;

        //先去掉空白
        classTime = classTime.replaceAll(" ", "");

        //把每天切開
        daylist = classTime.split("[日一二三四五六]");
        for (int j = 1; j < daylist.length; j++) {

            //把每節課分開
            timelist = daylist[j].split(",");

            //sort for time
            // 一10,7,8,9
            for (m = 0; m < timelist.length; m++) {
                if (timelist[m].length() <= 1) {
                    break;
                }
            }
            if (m != 0) {
                String[] tmp;
                tmp = new String[timelist.length];
                int idx = 0;
                for (; m < timelist.length; m++) {
                    tmp[idx++] = timelist[m];
                }
                for (m = 0; idx < timelist.length; idx++, m++) {
                    tmp[idx] = timelist[m];
                }
                for (idx = 0; idx < timelist.length; idx++) {
                    timelist[idx] = tmp[idx];
                }
            }

            for (; k < classTime.length(); k++) {
                //依序找到星期幾
                if ((processingTime = weekName.indexOf(classTime.substring(k, k + 1))) > -1) {
                    k++;
                    break;
                }
            }

            if (processingTime == -1) continue;

            mClass = timeTable.new Class(mClass);

            // 轉換時間代碼為 h:m
            convertClassTime(mClass, processingTime, timelist[0]);
            for (int l = 1; l < timelist.length; l++) {
                mergeClassTime(mClass, processingTime, timelist[l]);
            }

            timeTable.days[processingTime].classList.add(mClass);
        }

    }

    public static void convertClassTime(TimeTable.Class mClass, int dayofweek, String time) {
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

    public static Request<TimeTable, SemesterData> request() {
        Calendar today = Calendar.getInstance();
        int year, term;
        int month;

        year = today.get(Calendar.YEAR) - 1911 - 1;
        month = today.get(Calendar.MONTH);

        if(month >= Calendar.JULY) {
            term = 1;
            year++;
        } else if (month <= Calendar.JANUARY) {
            term = 1;
        } else term = 2;

        return request(year, term);
    }

    public static Request<TimeTable, SemesterData> request(int year, int term) {
        return new Request<>(TYPE, new SemesterData(year, term), TimeTable.class);
    }

    @Override
    public void initHTTPRequest(Request<TimeTable, SemesterData> request) {
        super.initHTTPRequest(request);

        SemesterData semesterData = request.args;
        HTTPParameter parameter = httpParameter(request);

        if (semesterData.year > 0 && semesterData.term > 0) {
            parameter.queryStrings("year", String.valueOf(semesterData.year));
            parameter.queryStrings("term", String.valueOf(semesterData.term));
        }
    }

    public static int randomColor(){
        return colors[new Random().nextInt(colors.length)];
    }

    @Override
    protected TimeTable parse(Request<TimeTable, SemesterData> request, HttpResponse response, Document document) throws Exception {
        Elements classes, fields, table;
        String fieldText,
                className = "",
                classTime = "",
                classRoom = "",
                classTeacher = "",
                classID = "",
                classCourseID = "";


        TimeTable result;
        TimeTable.Class mClass;

        String classNameHeader = "科目名稱"
                , classTimeHeader = "星期節次"
                , classRoomHeader = "教室"
                , classTeacherHeader = "授課教師"
                , classIDHeader = "班別"
                , classCourseIDHeader =	"科目代碼";

        int classNameIndex = -1
                , classTimeIndex = -1
                , classRoomIndex = -1
                , classTeacherIndex = -1
                , classIDIndex = -1
                , classCourseIDIndex = -1;

        int classColor, classColorTotal = 0;
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
            else if (classIDHeader.equals(fieldText))
                classIDIndex = i;
            else if (classCourseIDHeader.equals(fieldText))
                classCourseIDIndex = i;
        }

        for (int i = 1; i < classes.size(); i++) {

            fields = classes.get(i).select("th");

            if (classNameIndex >= 0) className = fields.get(classNameIndex).text();
            if (classRoomIndex >= 0) classRoom = fields.get(classRoomIndex).text();
            if (classTeacherIndex >= 0) classTeacher = fields.get(classTeacherIndex).text();
            if (classTimeIndex >= 0) classTime = fields.get(classTimeIndex).text();
            if (classIDIndex >= 0) classID = fields.get(classIDIndex).text();
            if (classCourseIDIndex >= 0) classCourseID = fields.get(classCourseIDIndex).text();
            classColor = randomColor();

            mClass = result.new Class();

            mClass.course_id = String.format("%s_%s", classCourseID, classID);
            mClass.name = className;
            mClass.classroom = classRoom;
            mClass.color = classColor;
            mClass.colorid = classColorTotal;
            mClass.teacher = classTeacher;
            parseClassTime(result, mClass, classTime);

            classColorTotal++;
        }
        result.sort();
        return result;
    }

    private static void mergeClassTime(TimeTable.Class mClass, int dayofweek, String time){
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
}

package org.zankio.cculife;

import android.content.Context;

import org.zankio.ccudata.kiki.model.Course;
import org.zankio.ccudata.kiki.model.TimeTable;
import org.zankio.ccudata.kiki.source.remote.TimetableSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static org.zankio.cculife.ui.CourseSchedule.TimeTableWeekFragment.randomColor;

public class KikiCourseAssest {

    public Context context;
    public KikiCourseAssest(Context context) {
        this.context = context;
    }

    //public final static String COURSE_FILE = "courses/10402";
    public final static String COURSE_FILE = "courses/10501";

    private String getAssetRawData(String fileName) {
        InputStream is;
        try {
            is = context.getAssets().open(fileName);
            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public Course[] getFindCourse(String key){
        key = key.toUpperCase();
        String[] data = getAssetRawData(COURSE_FILE).split("\n");
        ArrayList<Course> result = new ArrayList<>();

        for (String line : data) {
            if (line.toUpperCase().contains(key)) result.add(convertLineToCourse(line));
        }

        return result.toArray(new Course[result.size()]);
    }

    private Course convertLineToCourse(String line) {
        Course course = new Course();
        String[] field = line.split("\t");

        course.Dept = field[0];
        course.CourseID = field[2];
        course.ClassID = field[3];
        course.Name = field[4];
        course.Time = field[6];
        course.Teacher = field[7];
        course.ClassRoom = field[8];
        return course;
    }

    public static void addCourseToTimeTable(TimeTable timeTable, Course course) {
        TimeTable.Class result = timeTable.new Class();
        result.course_id = String.format("%s_%s", course.CourseID, course.ClassID);
        result.classroom = course.ClassRoom;
        result.teacher = course.Teacher;
        result.name = course.Name;
        result.color = randomColor();
        result.userAdd = 1;

        TimetableSource.parseClassTime(timeTable, result, course.Time);
    }

}

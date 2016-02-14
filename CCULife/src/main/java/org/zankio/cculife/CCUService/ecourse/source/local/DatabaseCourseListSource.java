package org.zankio.cculife.CCUService.ecourse.source.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.listener.IGetListener;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.listener.OnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.source.remote.CourseListSource;

import java.util.ArrayList;
import java.util.List;

import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_COURSEID;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_EXAM;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_HOMEWORK;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_NAME;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_NOTICE;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_TEACHER;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_WARNING;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.TABLE_ECOURSE;

public class DatabaseCourseListSource extends DatabaseBaseSource<Course[]> implements IGetListener {
    public final static String TYPE = CourseListSource.TYPE;
    public final static String[] DATA_TYPES = { TYPE };
    public final static SourceProperty property;
    static  {
        property = new SourceProperty(
                SourceProperty.Level.HIGH,
                SourceProperty.Level.MIDDLE,
                false,
                DATA_TYPES
        );
    }

    public DatabaseCourseListSource(BaseRepo context) {
        super(context, property);
    }

    private String[] ecourseColumns = {
            LIST_COLUMN_COURSEID,
            LIST_COLUMN_NAME,
            LIST_COLUMN_TEACHER,
            LIST_COLUMN_HOMEWORK,
            LIST_COLUMN_NOTICE,
            LIST_COLUMN_EXAM,
            LIST_COLUMN_WARNING
    };

    private Course cursorToCourse(Cursor cursor) {
        Course course = new Course((Ecourse) context);
        course.courseid = cursor.getString(0);
        course.name = cursor.getString(1);
        course.teacher = cursor.getString(2);
        course.homework = cursor.getInt(3);
        course.notice = cursor.getInt(4);
        course.exam = cursor.getInt(5);
        course.warning = cursor.getInt(6) == 1;
        return course;
    }

    public Course[] storeCourse(Course[] courses) {
        SQLiteDatabase database = getDatabase();
        if(courses == null || !database.isOpen() || database.isReadOnly()) return courses;

        database.delete(TABLE_ECOURSE, null, null);

        ContentValues values = new ContentValues();
        for(Course course : courses) {
            values.clear();
            values.put(LIST_COLUMN_COURSEID, course.courseid);
            values.put(LIST_COLUMN_NAME, course.name);
            values.put(LIST_COLUMN_TEACHER, course.teacher);
            values.put(LIST_COLUMN_HOMEWORK, course.homework);
            values.put(LIST_COLUMN_NOTICE, course.notice);
            values.put(LIST_COLUMN_EXAM, course.exam);
            values.put(LIST_COLUMN_WARNING, course.warning ? 1 : 0);
            database.insert(TABLE_ECOURSE, null, values);
        }

        return courses;
    }

    @Override
    public Course[] fetch(String type, Object... arg) throws Exception {
        SQLiteDatabase database = getDatabase();
        if(!database.isOpen()) return null;

        List<Course> result = new ArrayList<Course>();

        Cursor cursor = database.query(
                TABLE_ECOURSE,
                ecourseColumns,
                null, null, null, null, null
        );

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            result.add(cursorToCourse(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return result.size() > 0 ?
                result.toArray(new Course[result.size()]) :
                null;
    }

    @Override
    public void init() {
        super.init();

        for (String type : DATA_TYPES)
            context.registerUpdateListener(this, type);
    }

    @Override
    public IOnUpdateListener getListener(String type, Object... parameter) {
        return new OnUpdateListener<Course[]>() {
            @Override
            public void onNext(String type, Course[] courses, BaseSource source) {
                super.onNext(type, courses, source);
                if (source == null || source.getClass().equals(this.getClass())) return;

                if (TYPE.equals(type))
                    storeCourse(courses);
            }
        };
    }
}

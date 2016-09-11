package org.zankio.ccudata.ecourse.source.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Offline;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.source.remote.CourseListSource;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_COURSEID;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_EXAM;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_HOMEWORK;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_NAME;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_NOTICE;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_TEACHER;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.LIST_COLUMN_WARNING;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.TABLE_ECOURSE;

@DataType(DatabaseCourseListSource.TYPE)
@Order(SourceProperty.Level.HIGH)
@Important(SourceProperty.Level.MIDDLE)
@Offline

public class DatabaseCourseListSource extends DatabaseBaseSource<CourseData, Course[]>  {
    public final static String TYPE = CourseListSource.TYPE;

    private String[] ecourseColumns = {
            LIST_COLUMN_COURSEID,
            LIST_COLUMN_NAME,
            LIST_COLUMN_TEACHER,
            LIST_COLUMN_HOMEWORK,
            LIST_COLUMN_NOTICE,
            LIST_COLUMN_EXAM,
            LIST_COLUMN_WARNING
    };

    public DatabaseCourseListSource(Repository context) {
        super(context);
    }

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
        if (courses == null || !database.isOpen() || database.isReadOnly()) return courses;

        database.beginTransaction();
        try {
            database.delete(TABLE_ECOURSE, null, null);

            ContentValues values = new ContentValues();
            for (Course course : courses) {
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
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        return courses;
    }

    @Override
    public Course[] fetch(Request<Course[], CourseData> request) throws Exception {
        SQLiteDatabase database = getDatabase();
        if (!database.isOpen()) return null;

        List<Course> result = new ArrayList<>();

        Cursor cursor = database.query(
                TABLE_ECOURSE,
                ecourseColumns,
                null, null, null, null, null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
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

        context.registeOnNext(TYPE, listener());
    }

    private Repository.GetListener listener() {
        return () ->
                response -> {
                    Observable.just(response)
                            .subscribeOn(Schedulers.io())
                            // source not null
                            .filter(res -> res.request().source() != null)
                            // source not self
                            .filter(res -> !res.request().source().getClass().equals(DatabaseCourseListSource.this.getClass()))
                            // get data
                            .map(Response::data)
                            .ofType(Course[].class)
                            // call store function
                            // TODO: 2016/9/11 check error ?
                            .subscribe(this::storeCourse, Throwable::printStackTrace);

                    /*BaseSource source = response.request().source();
                    Course[] courses = (Course[]) response.data();

                    if (source == null || source.getClass().equals())
                        return;
                    new Thread(() -> {
                    }).start();*/
                };
    }
}

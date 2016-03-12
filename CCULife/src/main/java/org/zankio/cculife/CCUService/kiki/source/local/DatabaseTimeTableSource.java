package org.zankio.cculife.CCUService.kiki.source.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.listener.IGetListener;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.listener.OnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.kiki.model.TimeTable;
import org.zankio.cculife.CCUService.kiki.source.remote.TimetableSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper.TABLE_TIMETABLE;
import static org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper.TIME_COLUMN_CLASSROOM;
import static org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper.TIME_COLUMN_COLORID;
import static org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper.TIME_COLUMN_COURSEID;
import static org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper.TIME_COLUMN_DAYOFWEEK;
import static org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper.TIME_COLUMN_ENDTIME;
import static org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper.TIME_COLUMN_NAME;
import static org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper.TIME_COLUMN_STARTTIME;
import static org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper.TIME_COLUMN_TEACHER;
import static org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper.TIME_COLUMN_USERADD;
import static org.zankio.cculife.ui.CourseSchedule.TimeTableWeekFragment.randomColor;

public class DatabaseTimeTableSource extends DatabaseBaseSource<TimeTable> implements IGetListener {
    public final static String TYPE = TimetableSource.TYPE;
    public final static String TYPE_USERADD = "TIMETABLE_USERADD";
    public final static String[] DATA_TYPES = { TYPE, TYPE_USERADD};
    public final static SourceProperty property;
    static  {
        property = new SourceProperty(
                SourceProperty.Level.HIGH,
                SourceProperty.Level.MIDDLE,
                false,
                DATA_TYPES
        );
    }


    public DatabaseTimeTableSource(BaseRepo context) {
        super(context, property);
    }

    private final static String[] timetableColumn = {
            TIME_COLUMN_COURSEID,
            TIME_COLUMN_NAME,
            TIME_COLUMN_TEACHER,
            TIME_COLUMN_CLASSROOM,
            TIME_COLUMN_STARTTIME,
            TIME_COLUMN_ENDTIME,
            TIME_COLUMN_DAYOFWEEK,
            TIME_COLUMN_COLORID,
            TIME_COLUMN_USERADD
    };


    public String formatClassTime(Calendar calendar) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.US);
        return simpleDateFormat.format(calendar.getTime());
    }

    public Calendar parseClassTime(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.US);
        int hour, minute;
        Calendar calendar = new GregorianCalendar(0, 0, 0);
        try {
            calendar.setTime(simpleDateFormat.parse(time));
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);

            calendar.clear();
            calendar.set(0, 0, 0);
            calendar.set(Calendar.HOUR, hour);
            calendar.set(Calendar.MINUTE, minute);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    private TimeTable.Class cursorToClass(Cursor cursor, TimeTable timeTable) {
        TimeTable.Class mClass = timeTable.new Class();
        mClass.course_id = cursor.getString(0);
        mClass.name = cursor.getString(1);
        mClass.teacher = cursor.getString(2);
        mClass.classroom = cursor.getString(3);
        mClass.start = parseClassTime(cursor.getString(4));
        mClass.end = parseClassTime(cursor.getString(5));
        mClass.colorid = cursor.getInt(7);
        mClass.userAdd = cursor.getInt(8);

        return mClass;
    }

    public void storeTimeTable(TimeTable timeTable) {
        storeTimeTable(timeTable, false);
    }

    public void storeTimeTable(TimeTable timeTable, boolean all) {
        SQLiteDatabase database = getDatabase();
        if(timeTable == null || !database.isOpen() || database.isReadOnly()) return;
        database.beginTransaction();
        try {
            database.delete(TABLE_TIMETABLE, all ? null : TIME_COLUMN_USERADD + " = 0", null);

            ContentValues values = new ContentValues();

            TimeTable.Day[] days = timeTable.days;
            for (int i = 0; i < days.length; i++) {
                TimeTable.Day day = days[i];
                ArrayList<TimeTable.Class> classList = day.classList;
                for (TimeTable.Class mClass : classList) {
                    values.clear();
                    values.put(TIME_COLUMN_COURSEID, mClass.course_id);
                    values.put(TIME_COLUMN_NAME, mClass.name);
                    values.put(TIME_COLUMN_CLASSROOM, mClass.classroom);
                    values.put(TIME_COLUMN_TEACHER, mClass.teacher);
                    values.put(TIME_COLUMN_STARTTIME, formatClassTime(mClass.start));
                    values.put(TIME_COLUMN_ENDTIME, formatClassTime(mClass.end));
                    values.put(TIME_COLUMN_DAYOFWEEK, i);
                    values.put(TIME_COLUMN_COLORID, mClass.colorid);
                    values.put(TIME_COLUMN_USERADD, mClass.userAdd);

                    database.insert(TABLE_TIMETABLE, null, values);
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }


    @Override
    public TimeTable fetch(String type, Object... arg) throws Exception {
        SQLiteDatabase database = getDatabase();
        if(!database.isOpen()) return null;

        TimeTable result = new TimeTable();
        TimeTable.Class mClass;
        int dayOfWeek, size;
        int[] colors;

        String where;
        if (TYPE_USERADD.equals(type)) where = TIME_COLUMN_USERADD + " = 1";
        else
            where = TIME_COLUMN_USERADD + " = 0";

        Cursor cursor = database.query(
                TABLE_TIMETABLE,
                timetableColumn,
                where, null, null, null, null);

        cursor.moveToFirst();
        size = cursor.getCount();
        colors = new int[size];
        for (int i = 0; i < colors.length; i++) colors[i] = -1;
        while (!cursor.isAfterLast()) {
            mClass = cursorToClass(cursor, result);
            if (mClass.colorid >= colors.length)
                mClass.colorid = colors.length - 1;

            if (colors.length <= mClass.colorid || colors[mClass.colorid] < 0)
                colors[mClass.colorid] = randomColor();
            mClass.color = colors[mClass.colorid];

            dayOfWeek = cursor.getInt(6);

            result.days[dayOfWeek].classList.add(mClass);

            cursor.moveToNext();
        }
        cursor.close();
        result.sort();

        return size > 0 ? result : null;
    }

    @Override
    public void init() {
        super.init();

        for (String type : DATA_TYPES)
            context.registerUpdateListener(this, type);
    }

    @Override
    public IOnUpdateListener getListener(String type, Object... parameter) {
        return new OnUpdateListener<TimeTable>() {
            @Override
            public void onNext(String type, final TimeTable timeTable, BaseSource source) {
                super.onNext(type, timeTable, source);
                if (source == null || source.getClass().equals(DatabaseTimeTableSource.this.getClass())) return;

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (timeTable != null) storeTimeTable(timeTable);
                    }
                }).start();

            }
        };
    }

}

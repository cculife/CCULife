package org.zankio.cculife.CCUService.ecourse.source.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.listener.BindParamOnUpdateListener;
import org.zankio.cculife.CCUService.base.listener.IGetListener;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Announce;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.source.remote.AnnounceContentSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.AnnounceSource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_BROWSECOUNT;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_CONTENT;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_COURSEID;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_DATE;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_IMPORTANT;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_NEW;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_TITLE;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_URL;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.TABLE_ECOURSE_ANNOUNCE;

public class DatabaseAnnounceSource extends DatabaseBaseSource<Announce[]> implements IGetListener {
    public final static String TYPE_ANNOUNCE = AnnounceSource.TYPE;
    public final static String TYPE_ANNOUNCE_CONTENT = AnnounceContentSource.TYPE;
    public final static String[] DATA_TYPES = { TYPE_ANNOUNCE, TYPE_ANNOUNCE_CONTENT };
    public final static SourceProperty property;
    private final static String[] announceColumns = new String[]{
            ANNOUNCE_COLUMN_COURSEID,
            ANNOUNCE_COLUMN_TITLE,
            ANNOUNCE_COLUMN_CONTENT,
            ANNOUNCE_COLUMN_URL,
            ANNOUNCE_COLUMN_DATE,
            ANNOUNCE_COLUMN_BROWSECOUNT,
            ANNOUNCE_COLUMN_IMPORTANT,
            ANNOUNCE_COLUMN_NEW,
    };

    static  {
        property = new SourceProperty(
                SourceProperty.Level.HIGH,
                SourceProperty.Level.MIDDLE,
                false,
                DATA_TYPES
        );
    }

    public DatabaseAnnounceSource(BaseRepo context) {
        super(context, property);
    }

    @Override
    public void init() {
        super.init();

        for (String type : DATA_TYPES)
            context.registerUpdateListener(this, type);
    }

    public static void fetch(@NonNull Ecourse ecourse, IOnUpdateListener<Announce[]> listener, @NonNull Course course) {
        ecourse.fetch(DatabaseAnnounceSource.TYPE_ANNOUNCE, listener, course);
    }

    @Override
    public Announce[] fetch(String type, Object... arg) throws Exception {
        if (TYPE_ANNOUNCE_CONTENT.equals(type)) throw new Exception("未支援離線資料");
        if (arg.length < 1) throw new Exception("arg is miss");

        SQLiteDatabase database = getDatabase();
        if(!database.isOpen()) return null;

        Course course = (Course) arg[0];
        List<Announce> result = new ArrayList<>();

        Cursor cursor = database.query(
                TABLE_ECOURSE_ANNOUNCE,
                announceColumns,
                ANNOUNCE_COLUMN_COURSEID + "=\"" + course.courseid + "\"",
                null, null, null, ANNOUNCE_COLUMN_DATE + " DESC"
        );

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            result.add(cursorToAnnounce(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return result.size() > 0 ?
                result.toArray(new Announce[result.size()]) :
                null;
    }

    public String storeAnnounceContent(String content, Announce announce) {
        SQLiteDatabase database = getDatabase();
        if(content == null || !database.isOpen() || database.isReadOnly()) return content;

        ContentValues values = new ContentValues();
        values.put(ANNOUNCE_COLUMN_CONTENT, content);

        database.update(
                TABLE_ECOURSE_ANNOUNCE,
                values,
                ANNOUNCE_COLUMN_COURSEID + "=\"" + announce.getCourseID() + "\" AND " +
                        ANNOUNCE_COLUMN_URL + "=" + DatabaseUtils.sqlEscapeString(removeUrlPHPSESSID(announce.url)) + "",
                null
        );

        Cursor cursor = database.query(TABLE_ECOURSE_ANNOUNCE,
                announceColumns,
                ANNOUNCE_COLUMN_COURSEID + "=\"" + announce.getCourseID() + "\" AND " +
                        ANNOUNCE_COLUMN_URL + "=" + DatabaseUtils.sqlEscapeString(removeUrlPHPSESSID(announce.url)),
                null, null, null, null);

        cursor.moveToFirst();
        return content;
    }

    public Announce[] storeAnnounce(Announce[] announces, Course course) {
        SQLiteDatabase database = getDatabase();
        if(announces == null || !database.isOpen() || database.isReadOnly()) return announces;

        database.delete(
                TABLE_ECOURSE_ANNOUNCE,
                ANNOUNCE_COLUMN_COURSEID + "=\"" + course.courseid + "\"" +
                        " AND " + ANNOUNCE_COLUMN_CONTENT + " IS NULL OR " +
                        "trim(" + ANNOUNCE_COLUMN_CONTENT + ") = \"\"",
                null
        );

        Cursor cursor;

        ContentValues values = new ContentValues();
        for(Announce announce : announces) {
            values.clear();
            values.put(ANNOUNCE_COLUMN_TITLE, announce.title);
            values.put(ANNOUNCE_COLUMN_BROWSECOUNT, announce.browseCount);
            values.put(ANNOUNCE_COLUMN_COURSEID, announce.getCourseID());
            values.put(ANNOUNCE_COLUMN_DATE, announce.date);
            values.put(ANNOUNCE_COLUMN_IMPORTANT, announce.important);
            values.put(ANNOUNCE_COLUMN_NEW, announce.isnew ? 1 : 0);
            values.put(ANNOUNCE_COLUMN_URL, removeUrlPHPSESSID(announce.url));
            if(announce.content != null) values.put(ANNOUNCE_COLUMN_CONTENT, announce.content);

            cursor = database.query(
                    TABLE_ECOURSE_ANNOUNCE,
                    new String[]{ ANNOUNCE_COLUMN_URL },
                    ANNOUNCE_COLUMN_COURSEID + "=\"" + announce.getCourseID() + "\" AND " +
                            ANNOUNCE_COLUMN_URL + "=" + DatabaseUtils.sqlEscapeString(removeUrlPHPSESSID(announce.url)) + "",
                    null, null, null, null
            );

            if(cursor.getCount() > 0) {
                database.update(TABLE_ECOURSE_ANNOUNCE, values,
                        ANNOUNCE_COLUMN_COURSEID + "=\"" + announce.getCourseID() + "\" AND " +
                                ANNOUNCE_COLUMN_URL + "=" + DatabaseUtils.sqlEscapeString(removeUrlPHPSESSID(announce.url)) + "", null);
            } else {
                database.insert(TABLE_ECOURSE_ANNOUNCE, null, values);
            }

            cursor.close();

        }

        return announces;
    }

    private Announce cursorToAnnounce(Cursor cursor) {
        Course course = new Course((Ecourse) context);
        course.courseid = cursor.getString(0);

        Announce announce = new Announce((Ecourse) context, course);
        announce.title = cursor.getString(1);
        announce.content = cursor.getString(2);
        announce.url = cursor.getString(3);
        announce.date = cursor.getString(4);
        announce.browseCount = cursor.getInt(5);
        announce.important = cursor.getString(6);
        announce.isnew = cursor.getInt(7) == 1;
        return announce;
    }

    @Override
    public IOnUpdateListener getListener(String type, Object... parameter) {
        if (TYPE_ANNOUNCE.equals(type))
            return getAnnounceListener((Course) parameter[0]);
        else
            return getAnnounceContentListener((Announce) parameter[0]);
    }

    public IOnUpdateListener getAnnounceContentListener(Announce announce) {
        return new BindParamOnUpdateListener<Object, Announce>(announce) {
            @Override
            public void onNext(String type, Object data, BaseSource source) {
                super.onNext(type, data, source);
                if (source == null || source.getClass().equals(this.getClass())) return;

                storeAnnounceContent(parameter.content, parameter);
            }
        };
    }

    public IOnUpdateListener getAnnounceListener(Course course) {
        return new BindParamOnUpdateListener<Announce[], Course>(course) {
            @Override
            public void onNext(String type, final Announce[] announces, BaseSource source) {
                super.onNext(type, announces, source);
                if (source == null || source.getClass().equals(DatabaseAnnounceSource.this.getClass())) return;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        storeAnnounce(announces, parameter);
                    }
                }).start();
            }
        };
    }

    private String removeUrlPHPSESSID(String url) {
        return replaceAll(url, "((\\?|&)PHPSESSID=[^&]+&|(\\?|&)PHPSESSID=[^&]+$)", "$2");
    }

    private String replaceAll(String input, String regex, String replacement) {
        // Process substitution string to replace group references with groups
        int cursor = 0;
        Matcher matcher = Pattern.compile(regex).matcher(input);
        if(!matcher.find()) return input;

        StringBuilder result = new StringBuilder();

        while (cursor < replacement.length()) {
            char nextChar = replacement.charAt(cursor);
            if (nextChar == '\\') {
                cursor++;
                nextChar = replacement.charAt(cursor);
                result.append(nextChar);
                cursor++;
            } else if (nextChar == '$') {
                // Skip past $
                cursor++;
                // The first number is always a group
                int refNum = (int)replacement.charAt(cursor) - '0';
                if ((refNum < 0)||(refNum > 9))
                    throw new IllegalArgumentException(
                            "Illegal group reference");
                cursor++;

                // Capture the largest legal group string
                boolean done = false;
                while (!done) {
                    if (cursor >= replacement.length()) {
                        break;
                    }
                    int nextDigit = replacement.charAt(cursor) - '0';
                    if ((nextDigit < 0)||(nextDigit > 9)) { // not a number
                        break;
                    }
                    int newRefNum = (refNum * 10) + nextDigit;
                    if (matcher.groupCount() < newRefNum) {
                        done = true;
                    } else {
                        refNum = newRefNum;
                        cursor++;
                    }
                }
                // Append group
                if (matcher.group(refNum) != null)
                    result.append(matcher.group(refNum));
            } else {
                result.append(nextChar);
                cursor++;
            }
        }

        return matcher.replaceAll(result.toString());
    }
}

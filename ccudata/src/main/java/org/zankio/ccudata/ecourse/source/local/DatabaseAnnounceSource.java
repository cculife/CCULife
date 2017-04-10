package org.zankio.ccudata.ecourse.source.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.constant.Exceptions;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Offline;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.model.Announce;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.source.remote.AnnounceContentSource;
import org.zankio.ccudata.ecourse.source.remote.AnnounceSource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_BROWSECOUNT;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_CONTENT;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_COURSEID;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_DATE;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_IMPORTANT;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_NEW;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_TITLE;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.ANNOUNCE_COLUMN_URL;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.TABLE_ECOURSE_ANNOUNCE;

@Offline
@DataType({ DatabaseAnnounceSource.TYPE_ANNOUNCE, DatabaseAnnounceSource.TYPE_ANNOUNCE_CONTENT})
@Order(SourceProperty.Level.HIGH)
@Important(SourceProperty.Level.MIDDLE)
public class DatabaseAnnounceSource extends DatabaseBaseSource<CourseData, Announce[]> {
    public final static String TYPE_ANNOUNCE = AnnounceSource.TYPE;
    public final static String TYPE_ANNOUNCE_CONTENT = AnnounceContentSource.TYPE;
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

    public DatabaseAnnounceSource(Repository context) {
        super(context);
    }


    @Override
    public void init() {
        super.init();

        getContext().registeOnNext(TYPE_ANNOUNCE, listener());
        getContext().registeOnNext(TYPE_ANNOUNCE_CONTENT, listenerContent());
    }

    @Override
    public Announce[] fetch(Request<Announce[], CourseData> request) throws Exception {
        if (TYPE_ANNOUNCE_CONTENT.equals(request.type)) throw new Exception(Exceptions.NO_DATA);

        SQLiteDatabase database = getDatabase();
        if(!database.isOpen()) return null;

        Course course = request.args.course;
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

        return content;
    }

    public Announce[] storeAnnounce(Announce[] announces, Course course) {
        SQLiteDatabase database = getDatabase();
        if(announces == null || !database.isOpen() || database.isReadOnly()) return announces;

        database.beginTransaction();
        try {
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
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
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

    private Repository.GetListener listenerContent() {
        return () ->
                response -> {
                    BaseSource source = response.request().source();
                    Announce announce = (Announce) response.data();
                    if (source == null || source.getClass().equals(this.getClass())) return;

                    storeAnnounceContent(announce.content, announce);

                };
    }

    private Repository.GetListener listener() {
        return () ->
                response -> {
                    BaseSource source = response.request().source();
                    Announce[] announces = (Announce[]) response.data();
                    Course course = ((CourseData) response.request().args).course;

                    if (source == null || source.getClass().equals(DatabaseAnnounceSource.this.getClass())) return;

                    new Thread(() -> {
                        storeAnnounce(announces, course);
                    }).start();
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

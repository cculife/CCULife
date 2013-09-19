package org.zankio.cculife.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EcourseDatabaseHelper extends SQLiteOpenHelper{

    public static final String TABLE_ECOURSE = "courselist";
    public static final String TABLE_ECOURSE_ANNOUNCE = "announce";
    public static final String TABLE_ECOURSE_SCORE = "score";

    public static final String LIST_COLUMN_ID = "_id";
    public static final String LIST_COLUMN_NAME = "name";
    public static final String LIST_COLUMN_TEACHER = "teacher";
    public static final String LIST_COLUMN_COURSEID = "courseid";
    public static final String LIST_COLUMN_NOTICE = "notice";
    public static final String LIST_COLUMN_EXAM = "exam";
    public static final String LIST_COLUMN_HOMEWORK = "homework";
    public static final String LIST_COLUMN_WARNING = "warning";

    public static final String ANNOUNCE_COLUMN_ID = "_id";
    public static final String ANNOUNCE_COLUMN_COURSEID = "colurseid";
    public static final String ANNOUNCE_COLUMN_DATE = "date";
    public static final String ANNOUNCE_COLUMN_TITLE = "title";
    public static final String ANNOUNCE_COLUMN_Content = "content";
    public static final String ANNOUNCE_COLUMN_IMPORTANT = "important";
    public static final String ANNOUNCE_COLUMN_BROWSECOUNT = "browsecount";
    public static final String ANNOUNCE_COLUMN_URL = "url";

    public static final String SCORE_COLUMN_ID = "_id";
    public static final String SCORE_COLUMN_COURSEID = "courseid";
    public static final String SCORE_COLUMN_NAME = "name";
    public static final String SCORE_COLUMN_SCORE = "score";
    public static final String SCORE_COLUMN_RANK = "rank";
    public static final String SCORE_COLUMN_PERCENT = "percent";


    public static final String DATABASE_NAME = "ecourse.db";
    public static final int DATABASE_VERSION = 1;

    public EcourseDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE = "create table " +  TABLE_ECOURSE + "(" +
                LIST_COLUMN_ID + " integer primary key autoincrement, " +
                LIST_COLUMN_COURSEID + " text not null, " +
                LIST_COLUMN_NAME + " text not null, " +
                LIST_COLUMN_TEACHER + "text not null, " +
                LIST_COLUMN_NOTICE + " integer not null," +
                LIST_COLUMN_HOMEWORK + "integer not null, " +
                LIST_COLUMN_EXAM + "integer not null, " +
                LIST_COLUMN_WARNING + "integer not null);"


                + "create table " + TABLE_ECOURSE_ANNOUNCE + "(";

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

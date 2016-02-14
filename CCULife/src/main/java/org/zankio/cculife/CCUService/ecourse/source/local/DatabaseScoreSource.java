package org.zankio.cculife.CCUService.ecourse.source.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.listener.BindParamOnUpdateListener;
import org.zankio.cculife.CCUService.base.listener.IGetListener;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.model.Score;
import org.zankio.cculife.CCUService.ecourse.model.ScoreGroup;
import org.zankio.cculife.CCUService.ecourse.source.remote.ScoreSource;

import java.util.ArrayList;
import java.util.List;

import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_COURSEID;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_HEADER;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_NAME;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_PERCENT;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_RANK;
import static org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_SCORE;

public class DatabaseScoreSource
        extends DatabaseBaseSource<ScoreGroup[]>
        implements IGetListener {
    public final static String TYPE = ScoreSource.TYPE;
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

    public DatabaseScoreSource(BaseRepo context) {
        super(context, property);
    }

    private String[] scoreColumns = {
            SCORE_COLUMN_COURSEID,
            SCORE_COLUMN_NAME,
            SCORE_COLUMN_PERCENT,
            SCORE_COLUMN_RANK,
            SCORE_COLUMN_SCORE,
            SCORE_COLUMN_HEADER
    };

    private ScoreGroup cursorToScoreGroup(Cursor cursor) {
        ScoreGroup scoreGroup = new ScoreGroup();
        int idxName = cursor.getColumnIndex(SCORE_COLUMN_NAME),
                idxRank = cursor.getColumnIndex(SCORE_COLUMN_RANK),
                idxScore = cursor.getColumnIndex(SCORE_COLUMN_SCORE);

        scoreGroup.name = cursor.getString(idxName);
        scoreGroup.rank = cursor.getString(idxRank);
        scoreGroup.score = cursor.getString(idxScore);
        return scoreGroup;
    }

    private Score cursorToScore(Cursor cursor) {
        Score score = new Score();
        int idxName = cursor.getColumnIndex(SCORE_COLUMN_NAME),
                idxPercent = cursor.getColumnIndex(SCORE_COLUMN_PERCENT),
                idxRank = cursor.getColumnIndex(SCORE_COLUMN_RANK),
                idxScore = cursor.getColumnIndex(SCORE_COLUMN_SCORE);

        score.name = cursor.getString(idxName);
        score.percent = cursor.getString(idxPercent);
        score.rank = cursor.getString(idxRank);
        score.score = cursor.getString(idxScore);
        return score;
    }

    public ScoreGroup[] storeScoreGroup(ScoreGroup[] scores, Course course) {
        SQLiteDatabase database = getDatabase();
        if(scores == null || database == null || !database.isOpen() || database.isReadOnly()) return scores;

        int i = 1;

        database.delete(
                EcourseDatabaseHelper.TABLE_ECOURSE_SCORE,
                SCORE_COLUMN_COURSEID + "=\"" + course.courseid + "\"",
                null
        );

        ContentValues values = new ContentValues();
        for(ScoreGroup scoreHeader : scores) {
            values.clear();
            values.put(SCORE_COLUMN_NAME, scoreHeader.name);
            values.put(SCORE_COLUMN_SCORE, scoreHeader.score);
            values.put(SCORE_COLUMN_RANK, scoreHeader.rank);
            values.put(SCORE_COLUMN_COURSEID, course.courseid);
            values.put(SCORE_COLUMN_HEADER, -i);
            database.insert(EcourseDatabaseHelper.TABLE_ECOURSE_SCORE, null, values);

            if(scoreHeader.scores != null) {
                for(Score score: scoreHeader.scores) {
                    values.clear();
                    values.put(SCORE_COLUMN_NAME, score.name);
                    values.put(SCORE_COLUMN_SCORE, score.score);
                    values.put(SCORE_COLUMN_RANK, score.rank);
                    values.put(SCORE_COLUMN_PERCENT, score.percent);
                    values.put(SCORE_COLUMN_COURSEID, course.courseid);
                    values.put(SCORE_COLUMN_HEADER, i);
                    database.insert(EcourseDatabaseHelper.TABLE_ECOURSE_SCORE, null, values);
                }
            }
            i++;
        }

        return scores;
    }

    @Override
    public ScoreGroup[] fetch(String type, Object... arg) throws Exception {
        if (arg.length < 1) throw new Exception("arg is miss");

        SQLiteDatabase database = getDatabase();
        Course course = (Course) arg[0];
        if(!database.isOpen()) return null;

        List<ScoreGroup> ScoreGroup = new ArrayList<ScoreGroup>();
        List<Score> Score;
        Cursor cursorHeader, cursorScore;
        ScoreGroup now;

        cursorHeader = database.query(
                EcourseDatabaseHelper.TABLE_ECOURSE_SCORE,
                scoreColumns,
                SCORE_COLUMN_HEADER + "<0 AND " +
                        SCORE_COLUMN_COURSEID + "=\"" + course.courseid + "\"",
                null, null, null, null
        );

        cursorHeader.moveToFirst();
        while(!cursorHeader.isAfterLast()) {
            now = cursorToScoreGroup(cursorHeader);

            int idHeader = (-cursorHeader.getInt(cursorHeader.getColumnIndex(SCORE_COLUMN_HEADER)));
            cursorScore = database.query(
                    EcourseDatabaseHelper.TABLE_ECOURSE_SCORE,
                    scoreColumns,
                    SCORE_COLUMN_HEADER + "=" + idHeader + " AND " +
                            SCORE_COLUMN_COURSEID + "=\"" + course.courseid + "\"",
                    null, null, null, null
            );

            Score = new ArrayList<>();

            cursorScore.moveToFirst();
            while(!cursorScore.isAfterLast()) {
                Score.add(cursorToScore(cursorScore));
                cursorScore.moveToNext();
            }

            now.scores = Score.toArray(new Score[Score.size()]);

            ScoreGroup.add(now);
            cursorScore.close();
            cursorHeader.moveToNext();
        }
        cursorHeader.close();

        return ScoreGroup.size() > 0 ?
                ScoreGroup.toArray(new ScoreGroup[ScoreGroup.size()]) :
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
        return new BindParamOnUpdateListener<ScoreGroup[], Course>((Course) parameter[0]) {
            @Override
            public void onNext(String type, ScoreGroup[] data, BaseSource source) {
                super.onNext(type, data, source);
                if (source == null || source.getClass().equals(this.getClass())) return;

                storeScoreGroup(data, this.parameter);
            }
        };
    }
}

package org.zankio.ccudata.ecourse.source.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Offline;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.model.Score;
import org.zankio.ccudata.ecourse.model.ScoreGroup;
import org.zankio.ccudata.ecourse.source.remote.ScoreSource;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_COURSEID;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_HEADER;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_NAME;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_PERCENT;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_RANK;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.SCORE_COLUMN_SCORE;
import static org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper.TABLE_ECOURSE_SCORE;

@Offline
@DataType(DatabaseScoreSource.TYPE)
@Order(SourceProperty.Level.HIGH)
@Important(SourceProperty.Level.MIDDLE)
public class DatabaseScoreSource
        extends DatabaseBaseSource<CourseData, ScoreGroup[]> {

    public final static String TYPE = ScoreSource.TYPE;
    private String[] scoreColumns = new String[]{
            SCORE_COLUMN_COURSEID,
            SCORE_COLUMN_NAME,
            SCORE_COLUMN_PERCENT,
            SCORE_COLUMN_RANK,
            SCORE_COLUMN_SCORE,
            SCORE_COLUMN_HEADER
    };

    public DatabaseScoreSource(Repository context) {
        super(context);
    }

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
        database.beginTransaction();

        try {
            database.delete(
                    TABLE_ECOURSE_SCORE,
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
                database.insert(TABLE_ECOURSE_SCORE, null, values);

                if(scoreHeader.scores != null) {
                    for(Score score: scoreHeader.scores) {
                        values.clear();
                        values.put(SCORE_COLUMN_NAME, score.name);
                        values.put(SCORE_COLUMN_SCORE, score.score);
                        values.put(SCORE_COLUMN_RANK, score.rank);
                        values.put(SCORE_COLUMN_PERCENT, score.percent);
                        values.put(SCORE_COLUMN_COURSEID, course.courseid);
                        values.put(SCORE_COLUMN_HEADER, i);
                        database.insert(TABLE_ECOURSE_SCORE, null, values);
                    }
                }
                i++;
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        return scores;
    }

    @Override
    public ScoreGroup[] fetch(Request<ScoreGroup[], CourseData> request) throws Exception {
        SQLiteDatabase database = getDatabase();
        Course course = request.args.course;
        if(!database.isOpen()) return null;

        List<ScoreGroup> ScoreGroup = new ArrayList<>();
        List<Score> Score;
        Cursor cursorHeader, cursorScore;
        ScoreGroup now;

        cursorHeader = database.query(
                TABLE_ECOURSE_SCORE,
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
                    TABLE_ECOURSE_SCORE,
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
                            .filter(res -> !res.request().source().getClass().equals(DatabaseScoreSource.this.getClass()))
                            .subscribe(
                                    res -> {
                                        storeScoreGroup((ScoreGroup[]) res.data(), ((CourseData)res.request().args).course);
                                    },
                                    // TODO: 2016/9/11 check error
                                    Throwable::printStackTrace
                            );
                };
    }
}

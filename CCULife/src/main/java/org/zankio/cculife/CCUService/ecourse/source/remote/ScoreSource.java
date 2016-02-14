package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.model.Score;
import org.zankio.cculife.CCUService.ecourse.model.ScoreGroup;

import java.io.IOException;
import java.util.ArrayList;

public class ScoreSource extends CourseSource<ScoreGroup[]> {
    public final static String TYPE = "SCORE";
    public final static String[] DATA_TYPES = { TYPE };
    public final static SourceProperty property;
    static  {
        property = new SourceProperty(
                SourceProperty.Level.MIDDLE,
                SourceProperty.Level.HIGH,
                false,
                DATA_TYPES
        );
    }

    public ScoreSource(Ecourse context) {
        super(context, property);
    }

    public static void fetch(Ecourse context, IOnUpdateListener listener, Course course) {
        context.fetch(TYPE, listener, course);
    }

    @Override
    protected String getUrl(Course course) {
        return Url.COURSE_SCORE;
    }

    @Override
    protected Document execute(Connection connection) throws IOException {
        connection.method(Connection.Method.GET);
        return Jsoup.parse(new String(connection.execute().bodyAsBytes(), "big5"));
    }

    @Override
    public ScoreGroup[] parse(Document document, Course course) {
        Elements scores, fields;
        ArrayList<ScoreGroup> result;
        ArrayList<Score> score = null;
        ScoreGroup mScoreGroup = null;
        Score mScore;

        scores = document.select("tr[bgcolor=#4d6eb2], tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");

        result = new ArrayList<>();

        for(int i = 0; i < scores.size(); i++) {
            fields = scores.get(i).select("td");

            // Name row check
            if ("#4d6eb2".equals(scores.get(i).attr("bgcolor"))) {
                if (mScoreGroup != null && score.size() > 0) {
                    mScoreGroup.scores = score.toArray(new Score[score.size()]);
                    result.add(mScoreGroup);
                }

                mScoreGroup = new ScoreGroup();
                mScoreGroup.name = fields.get(0).text().replace("(名稱)", "");
                score = new ArrayList<>();
                continue;
            }

            mScore = new Score();
            mScore.name = fields.get(0).text();
            mScore.percent = fields.get(1).text();
            mScore.score = fields.get(2).text();
            mScore.rank = fields.get(3).text();

            assert score != null;
            score.add(mScore);
        }

        assert score != null;
        if (score.size() > 0) {
            mScoreGroup.scores = score.toArray(new Score[score.size()]);
            result.add(mScoreGroup);
        }

        mScoreGroup = new ScoreGroup();
        mScoreGroup.name = "總分";
        scores = document.select("tr[bgcolor=#B0BFC3]");
        if (scores.size() >= 2) {
            fields = scores.get(0).select("th");
            if (fields.size() >= 2) mScoreGroup.rank = fields.get(1).text();
            fields = scores.get(1).select("th");
            if (fields.size() >= 2) mScoreGroup.score = fields.get(1).text();
        }

        if (mScoreGroup.rank != null && !"你沒有成績".equals(mScoreGroup.rank)) result.add(mScoreGroup);


        return result.toArray(new ScoreGroup[result.size()]);
    }
}

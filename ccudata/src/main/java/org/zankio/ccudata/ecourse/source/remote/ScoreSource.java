package org.zankio.ccudata.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.annotation.Charset;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.base.source.http.annotation.Url;
import org.zankio.ccudata.ecourse.annotation.ChangeCourse;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.model.Score;
import org.zankio.ccudata.ecourse.model.ScoreGroup;

import java.util.ArrayList;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@Url(Urls.COURSE_SCORE)
@Charset("big5")

@DataType(ScoreSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@ChangeCourse
public class ScoreSource extends EcourseSource<CourseData, ScoreGroup[]> {
    public final static String TYPE = "SCORE";
    public static Request<ScoreGroup[], CourseData> request(Course course) {
        return new Request<>(TYPE, new CourseData(course), ScoreGroup[].class);
    }

    @Override
    protected ScoreGroup[] parse(Request<ScoreGroup[], CourseData> request, HttpResponse response, Document document) throws Exception {
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

            if (mScore.score.isEmpty())
                mScore.score = "-";

            if (mScore.rank.equals("你沒有成績"))
                mScore.rank = "-";

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

package org.zankio.ccudata.sourcequery;

import org.junit.Test;
import org.zankio.ccudata.sourcequery.model.Grade;
import org.zankio.ccudata.sourcequery.model.Score;
import org.zankio.ccudata.sourcequery.source.remote.GradesInquiriesSource;

public class ScoreQueryTest {

    private static final String username = "401234567";
    private static final String password = "qwertyuio";

    @Test
    public void testFetch() throws Exception {
        Grade[] grades = new ScoreQuery(null)
                .fetch(GradesInquiriesSource.request(username, password))
                .toBlocking()
                .single()
                .data();

        for (Grade grade : grades) {
            System.out.println(grade.grade + " " + grade.description);
            for (Score score : grade.scores) {
                System.out.println(String.format(
                        "%s %s %s %s %s %s",
                        score.coruseID,
                        score.classID,
                        score.courseName,
                        score.credit,
                        score.creditType,
                        score.score
                ));
            }

        }


    }
}

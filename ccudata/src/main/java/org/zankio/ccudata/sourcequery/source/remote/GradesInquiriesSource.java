package org.zankio.ccudata.sourcequery.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.FetchParseSource;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.model.AuthData;
import org.zankio.ccudata.sourcequery.model.Grade;
import org.zankio.ccudata.sourcequery.model.Score;

@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@DataType(GradesInquiriesSource.TYPE)
public class GradesInquiriesSource extends FetchParseSource<AuthData, Grade[], Document> {
    public final static String TYPE = "GRADES_INQUIRIES";

    public static Request<Grade[], AuthData> request(String username, String password) {
        return new Request<>(TYPE, new AuthData(username, password), Grade[].class);
    }

    @Override
    protected Grade[] parse(Request<Grade[], AuthData> request, Document response) throws Exception {
        if (response == null) return new Grade[0];

        Elements grades, scores, fields;
        Element scoreTable;
        Node description;
        Grade[] result;

        grades = response.select("h3");
        result = new Grade[grades.size()];

        for (int i = 0; i < grades.size(); i++) {
            result[i] = new Grade();
            result[i].grade = grades.get(i).text();

            scoreTable = grades.get(i).nextElementSibling();
            description = scoreTable.nextSibling();
            result[i].description = description.outerHtml();

            if (scoreTable == null) continue;

            scores = scoreTable.select("tr");
            result[i].scores = new Score[scores.size() - 1];

            for (int j = 1; j < scores.size(); j++) {
                fields = scores.get(j).select("td");

                result[i].scores[j - 1] = new Score();
                result[i].scores[j - 1].coruseID = fields.get(0).text();
                result[i].scores[j - 1].classID = fields.get(1).text();
                result[i].scores[j - 1].courseName = fields.get(2).text();
                result[i].scores[j - 1].creditType = fields.get(3).text();
                result[i].scores[j - 1].credit = fields.get(4).text();
                result[i].scores[j - 1].score = fields.get(5).text();

            }
        }

        return result;
    }

    @Override
    protected Document fetch(Request<Grade[], AuthData> request, boolean inner) throws Exception {
        Document cacheDocument = Authenticate.cacheDocument(getContext());
        AuthData authData = request.args;

        if (cacheDocument == null) {
            getContext()
                    .fetch(Authenticate.request(authData.username, authData.password))
                    .toBlocking()
                    .single();
            cacheDocument = Authenticate.cacheDocument(getContext());
        }

        Authenticate.cacheDocument(getContext(), null);
        return cacheDocument;
    }
}

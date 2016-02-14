package org.zankio.cculife.CCUService.sourcequery.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.sourcequery.ScoreQueryNew;
import org.zankio.cculife.CCUService.sourcequery.model.Grade;
import org.zankio.cculife.CCUService.sourcequery.model.Score;

public class GradesInquiriesSource extends BaseSource<Grade[]>{
    public final static String TYPE = "GRADES_INQUIRIES";
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

    public GradesInquiriesSource(ScoreQueryNew context) {
        super(context, property);
    }

    public Grade[] parserGrade(Document document) {
        Elements grades, scores, fields;
        Element scoreTable;
        Node description;
        Grade[] result;

        grades = document.select("h3");
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
    public Grade[] fetch(String type, Object... arg) throws Exception {
        ScoreQueryNew context = (ScoreQueryNew) this.context;
        BaseSession<Document> session = context.getSession();
        if (session == null) throw new Exception("Session is miss");
        if (!session.isAuthenticated() || session.getIdentity() == null)
            context.fetchSync(Authenticate.TYPE, context.getUsername(), context.getPassword());

        Document document = session.getIdentity();

        //reload
        session.setAuthenticated(false);
        session.setIdentity(null);
        if (document != null) return parserGrade(document);
        return new Grade[0];
    }
}

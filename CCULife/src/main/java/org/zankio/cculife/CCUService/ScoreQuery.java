package org.zankio.cculife.CCUService;


import android.content.Context;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;


// Data http://140.123.30.107/~ccmisp06/cgi-bin/Query/
public class ScoreQuery extends BaseService {

    private Context context;

    private Document data;
    private Grade[] grades;

    public ScoreQuery(Context context) {
        this.context = context;
    }

    @Override
    public boolean getSession() throws Exception {
        SessionManager sessionManager;
        sessionManager = new SessionManager(context);

        if (!sessionManager.isLogined()) throw Exceptions.getNeedLoginException();

        Connection connection;
        Document document;
        Elements grades, scores;

        //ToDo remove? HotFix 140.123.30.107 NoResponse
        connection = Jsoup.connect("http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/Query/Query_grade.php");
        connection.data("id", sessionManager.getUserName())
                  .data("password", sessionManager.getPassword());

        try {
            document = connection.post();
            if (document.select("table").size() != 0) {
                SESSIONID = connection.response().cookie("PHPSESSID");
                data = document;
                return true;
            }

            return false;
        } catch (IOException e) {
            throw Exceptions.getNetworkException(e);
        }


    }

    public Grade[] getGrades() {
        if (grades != null) return grades;

        if (data == null) {
            if (SESSIONID == null || SESSIONID.equals("")) {
                return null;
            }
        }

        Elements grades, scores, fields;
        Element scoreTable;
        Node description;
        Grade[] result = null;

        grades = data.select("h3");
        result = new Grade[grades.size()];

        for (int i = 0; i < grades.size(); i++) {
            result[i] = new Grade();
            result[i].Grade = grades.get(i).text();

            scoreTable = grades.get(i).nextElementSibling();
            description = scoreTable.nextSibling();
            result[i].Description = description.outerHtml();

            if (scoreTable == null) continue;

            scores = scoreTable.select("tr");
            result[i].Scores = new Score[scores.size() - 1];

            for (int j = 1; j < scores.size(); j++) {
                fields = scores.get(j).select("td");

                result[i].Scores[j - 1] = new Score();
                result[i].Scores[j - 1].CoruseID = fields.get(0).text();
                result[i].Scores[j - 1].ClassID = fields.get(1).text();
                result[i].Scores[j - 1].CourseName = fields.get(2).text();
                result[i].Scores[j - 1].CreditType = fields.get(3).text();
                result[i].Scores[j - 1].Credit = fields.get(4).text();
                result[i].Scores[j - 1].Score = fields.get(5).text();

            }
        }
        this.grades = result;
        return result;

    }

    public class Grade {

        public Score[] Scores;
        public String Grade;
        public String Description;
    }

    public class Score {
        public String CoruseID;
        public String ClassID;
        public String CourseName;
        public String CreditType;
        public String Credit;
        public String Score;
    }
}

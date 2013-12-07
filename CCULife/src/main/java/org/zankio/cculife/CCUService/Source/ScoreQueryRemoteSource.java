package org.zankio.cculife.CCUService.Source;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.zankio.cculife.CCUService.Authentication.CookieAuth;
import org.zankio.cculife.CCUService.Helper.ConnectionHelper;
import org.zankio.cculife.CCUService.Parser.ScoreQueryParser;
import org.zankio.cculife.CCUService.ScoreQuery;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;

public class ScoreQueryRemoteSource extends ScoreQuerySource{

    private CookieAuth auth;
    private ScoreQueryParser parser;
    private Document data;
    private final static String SESSION_FIELD_NAME = "PHPSESSID";

    public ScoreQueryRemoteSource(ScoreQueryParser parser) {
        this.auth = new CookieAuth();
        this.parser = parser;
    }

    private void checkAuth() throws Exception {
        if(auth.getCookie(SESSION_FIELD_NAME) == null) {
            throw Exceptions.getNeedLoginException();
        }
    }

    @Override
    public boolean Authentication(SessionManager sessionManager) throws Exception {
        if (!sessionManager.isLogined()) throw Exceptions.getNeedLoginException();

        Connection connection;
        Document document;

        connection = Jsoup.connect("http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/Query/Query_grade.php");
        ConnectionHelper.initTimeout(connection);

        connection.data("id", sessionManager.getUserName())
                .data("password", sessionManager.getPassword());

        try {
            document = connection.post();
            if (document.select("table").size() != 0) {
                data = document;
                auth.setCookie(connection, SESSION_FIELD_NAME);
                return true;
            }
        } catch (IOException e) {
            throw Exceptions.getNetworkException(e);
        }
        return false;
    }

    @Override
    public ScoreQuery.Grade[] getGrades() throws Exception {
        checkAuth();

        if (data == null)
            return null;

        return parser.parserGrade(data);
    }
}

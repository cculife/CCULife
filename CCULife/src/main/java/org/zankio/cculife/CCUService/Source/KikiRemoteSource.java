package org.zankio.cculife.CCUService.Source;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.zankio.cculife.CCUService.Authentication.QueryStringAuth;
import org.zankio.cculife.CCUService.Helper.ConnectionHelper;
import org.zankio.cculife.CCUService.Kiki;
import org.zankio.cculife.CCUService.Parser.IParser;
import org.zankio.cculife.CCUService.Parser.KikiParser;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.LoginErrorException;

import java.io.IOException;
import java.util.Calendar;

public class KikiRemoteSource extends KikiSource {

    private Kiki kiki;
    private QueryStringAuth auth;
    private KikiParser parser;
    private ConnectionHelper connectionHelper;
    private final static String SESSION_FIELD_NAME = "session_id";

    public KikiRemoteSource(Kiki kiki, IParser parser) {
        this.kiki = kiki;
        this.auth = new QueryStringAuth();
        this.parser = (KikiParser) parser;
        this.connectionHelper = new ConnectionHelper(auth);
    }

    public void checkAuth() throws Exception {
        if(auth.getQueryParameter(SESSION_FIELD_NAME) == null)
            throw Exceptions.getNeedLoginException();
    }

    public boolean Authenticate(String user, String pass) throws Exception {
        String sessionID, error;
        Document document;
        Connection connection;

        connection = Jsoup.connect("http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/class_new/bookmark.php");
        ConnectionHelper.initTimeout(connection);
        connection.data("id", user)
                .data("password", pass)
                .data("term", "on");

        try {
            document = connection.post();
            sessionID = parser.parserSessionID(document);

            if (sessionID == null) {
                error = parser.parserError(document);

                if (error != null) {
                    if (error.contains("學號應為九碼半形數字") ||
                            error.contains("請輸入正確學號") ||
                            error.contains("密碼長度過長或過短") ||
                            error.contains("您輸入的密碼有誤"))
                    {
                        throw new LoginErrorException("帳號或密碼錯誤");
                    } else {
                        throw new LoginErrorException("未知錯誤");
                    }
                }
                return false;
            }
            auth.addQueryParameter(SESSION_FIELD_NAME, sessionID);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }
    }

    public boolean Authenticate(SessionManager sessionManager) throws Exception {
        if (!sessionManager.isLogined()) throw Exceptions.getNeedLoginException();
        return Authenticate(sessionManager.getUserName(), sessionManager.getPassword());
    }

    public Kiki.TimeTable getTimeTable() throws Exception {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR) - 1911 - 1;
        int month = today.get(Calendar.MONTH);
        int term;


        if(month >= Calendar.JULY) {
            term = 1;
            year++;
        } else if(month <= Calendar.JANUARY) {
            term = 1;
        }
        else term = 2;
        return getTimeTable(year, term);

    }

    public Kiki.TimeTable getTimeTable(int year, int term) throws Exception {
        checkAuth();

        Connection connection;
        connection = Jsoup.connect("http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/class_new/Selected_View00.cgi?" +
                (year > 0 && term > 0 ? ("year=" + year + "&term=" + term) : ""));
        connectionHelper.initConnection(connection);

        try {
            return parser.parserTimeTable(kiki, connection.get());
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

    }

    public Kiki.Course[] getCourseList(int year, int term) throws Exception {
        checkAuth();

        Connection connection;
        connection = Jsoup.connect("http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/class_new/Selected_View00.cgi?"+
                (year > 0 && term > 0 ? ("year=" + year + "&term=" + term) : ""));
        connectionHelper.initConnection(connection);

        try {
            return parser.parseCourseList(year, term, connection.get());
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

    }
}

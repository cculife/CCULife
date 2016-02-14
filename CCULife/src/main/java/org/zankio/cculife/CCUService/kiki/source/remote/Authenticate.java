package org.zankio.cculife.CCUService.kiki.source.remote;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.authentication.QueryStringAuth;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.kiki.Kiki;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.LoginErrorException;

import java.io.IOException;

public class Authenticate extends BaseSource<Boolean> {
    private static final String SESSION_FIELD_NAME = "session_id";
    public final static String TYPE = "AUTH";
    public final static String[] DATA_TYPES = {TYPE};
    public final static SourceProperty property;

    static {
        property = new SourceProperty(
            SourceProperty.Level.HIGH,
            SourceProperty.Level.HIGH,
            false,
            DATA_TYPES
        );
    }

    public Authenticate(Kiki context) {
        super(context, property);
    }

    public String parserSessionID(Document document) {
        Elements logout;
        String sessionid;
        int q;

        logout = document.select("a[href^=logout.php?]");
        if (logout.size() == 0) return null;

        sessionid = logout.attr("href");
        q = sessionid.indexOf("session_id=");
        sessionid = sessionid.substring(q + 11);
        q = sessionid.indexOf("&");
        if(q >= 0) sessionid = sessionid.substring(0, q);

        return sessionid;
    }

    public String parserError(Document document) {
        StringBuilder message = new StringBuilder();
        Elements textNode;

        textNode = document.select("font");
        for (int i = 0; i < textNode.size(); i++) {
            message.append(textNode.get(i).text());
        }

        return message.toString();
    }

    @Override
    public Boolean fetch(String type, Object ...arg) throws Exception {
        if (arg.length < 2) throw new Exception("arg miss!?");

        Kiki context = (Kiki) this.context;
        String user = (String) arg[0],
                pass = (String) arg[1];

        QueryStringAuth auth = null;
        BaseSession session = context.getSession();
        if (session != null) {
            auth = (QueryStringAuth) session.getAuth();
        }

        Connection connection = context.buildConnection("https://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/class_new/bookmark.php")
                .data("id", user)
                .data("password", pass)
                .data("term", "on");

        String sessionID, error;
        Document document;
        try {
            document = connection.post();
            sessionID = parserSessionID(document);

            if (sessionID == null) {
                error = parserError(document);

                if (error != null) {
                    if (error.contains("學號應為九碼半形數字") ||
                            error.contains("請輸入正確學號") ||
                            error.contains("密碼長度過長或過短") ||
                            error.contains("您輸入的密碼有誤"))
                    {
                        throw new LoginErrorException("帳號或密碼錯誤");
                    } else {
                        throw new LoginErrorException("未知錯誤 : " + error);
                    }
                }
                throw new LoginErrorException("未知錯誤");
            }
            if (auth != null) {
                auth.addQueryParameter(SESSION_FIELD_NAME, sessionID);
                session.setAuthenticated(true);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }
    }
}

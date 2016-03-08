package org.zankio.cculife.CCUService.sourcequery.source.remote;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.sourcequery.ScoreQueryNew;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.LoginErrorException;

import java.io.IOException;

public class Authenticate extends BaseSource<Boolean> {
    public final static String TYPE = "AUTH";
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

    public Authenticate(ScoreQueryNew context) {
        super(context, property);
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
    public Boolean fetch(String type, Object... arg) throws Exception {
        if (arg.length < 2) throw new Exception("arg miss!?");

        String user = (String) arg[0],
                pass = (String) arg[1];

        ScoreQueryNew context = (ScoreQueryNew) this.context;
        BaseSession<Document> session = context.getSession();
        if (session == null) throw new Exception("Session is miss");

        Connection connection = context.buildConnection("https://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/Query/Query_grade.php");
        Document document;
        String error;

        connection.data("id", user)
                .data("password", pass);

        try {
            document = connection.post();

            if (document.select("table").size() != 0) {
                session.setIdentity(document);
                return true;
            } else {
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
                return false;
            }
        } catch (IOException e) {
            throw Exceptions.getNetworkException(e);
        }
    }
}

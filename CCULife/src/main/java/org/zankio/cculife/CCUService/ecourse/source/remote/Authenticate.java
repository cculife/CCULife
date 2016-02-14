package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.Connection;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.authentication.CookieAuth;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.LoginErrorException;

import java.io.IOException;

public class Authenticate extends BaseSource<Boolean> {
    private static final String SESSION_FIELD_NAME = "PHPSESSID";
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

    public Authenticate(Ecourse context) {
        super(context, property);
    }

    @Override
    public Boolean fetch(String type, Object ...arg) throws Exception {
        if (arg.length < 2) throw new Exception("arg miss!?");

        Ecourse context = (Ecourse) this.context;
        String user = (String) arg[0],
                pass = (String) arg[1];

        CookieAuth auth = null;
        BaseSession session = context.getSession();
        if (session != null) {
            auth = (CookieAuth) session.getAuth();
        }

        Connection connection = context.buildConnection(Url.LOGIN)
                .data("id", user)
                .data("pass", pass)
                .data("ver", "C");

        try {
            connection.post();
            String url = connection.response().url().toString();
            String body = connection.response().body();

            if (url.startsWith("https://ecourse.ccu.edu.tw/php/Courses_Admin/take_course.php")) {
                if (auth != null) {
                    auth.setCookie(connection, SESSION_FIELD_NAME);
                    session.setAuthenticated(true);
                }
                return true;
            } else if (url.startsWith(Url.LOGIN)) {
                if (body != null) {
                    if (body.contains("帳號或密碼錯誤")) {
                        throw new LoginErrorException("帳號或密碼錯誤");
                    }
                }
            }
            throw new LoginErrorException("未知錯誤");
        } catch (IOException e) {
            throw Exceptions.getNetworkException(e);
        }
    }
}

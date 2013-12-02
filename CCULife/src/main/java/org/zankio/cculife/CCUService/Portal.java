package org.zankio.cculife.CCUService;

import android.content.Context;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.zankio.cculife.CCUService.PortalService.BasePortal;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.Net;

import java.io.IOException;
import java.util.HashMap;

public class Portal extends BaseService {

    public Context context;
    private static HashMap<String, String> SSO_URL;
    private static final String SSO_URL_BASE = "http://portal.ccu.edu.tw/ssoService.php?service=%s&linkId=%s";
    public static final String SSO_ECOURSE = "0000";
    public static final String SSO_SCORE = "0007";
    private static final String SSO_ECOURSE_URL = "http://ecourse.elearning.ccu.edu.tw/php/getssoCcuRight.php";
    private static final String SSO_SCORE_URL = "http://140.123.30.106/~ccmisp06/cgi-bin/library/SSO/Query_grade/getssoCcuRight.php";


    static {
        SSO_URL = new HashMap<String, String>();
        SSO_URL.put(SSO_ECOURSE, SSO_ECOURSE_URL);
        SSO_URL.put(SSO_SCORE, SSO_SCORE_URL);
    }

    public Portal(Context context) {
        this.context = context;
    }

    @Override
    public boolean getSession() throws Exception {
        SessionManager sessionManager = SessionManager.getInstance(context);
        Connection connection;
        Document document;
        String location;
        String cookie;
        try {
            connection = Jsoup.connect("http://portal.ccu.edu.tw/").timeout(Net.CONNECT_TIMEOUT);
            connection.get();
            cookie = connection.response().cookies().get("ccuSSO");
            connection.cookie("ccuSSO", cookie)
                    .url("http://portal.ccu.edu.tw/login_check.php")
                    .data("acc", sessionManager.getUserName())
                    .data("pass", sessionManager.getPassword())
                    .data("authcode", "請輸入右邊文字");
            connection.followRedirects(false);
            connection.post();

            location = connection.response().header("Location");
            if (location != null && location.equals("http://portal.ccu.edu.tw/sso_index.php")) {
                SESSIONID = cookie;
                return true;
            }

            SESSIONID = null;
            return false;

        } catch (IOException e){
            throw Exceptions.getNetworkException(e);
        }
    }

    public String[] getSSOPortal(BasePortal portal) throws Exception {
        if (SESSIONID == null) throw new Exception("登入錯誤");

        Connection connection;

        String ssoURL = portal.getSSOPortalURL();
        String location;

        connection = Jsoup.connect(ssoURL).timeout(Net.CONNECT_TIMEOUT);

        connection.followRedirects(false)
                .cookie("ccuSSO", SESSIONID);

        try {
            connection.execute();
            location = connection.response().header("Location");

            if (location != null) {
                return portal.onPostExcute(location);
            }

        } catch (IOException e) {
            throw Exceptions.getNetworkException(e);
        }

        return null;
    }
}

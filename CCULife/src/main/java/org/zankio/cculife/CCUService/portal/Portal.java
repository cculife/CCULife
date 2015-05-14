package org.zankio.cculife.CCUService.portal;

import android.content.Context;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.zankio.cculife.CCUService.base.BaseService;
import org.zankio.cculife.CCUService.portal.service.BasePortal;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.LoginErrorException;
import org.zankio.cculife.override.Net;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Portal extends BaseService {

    public Context context;
    private static HashMap<String, String> SSO_URL;
    private static final String SSO_URL_BASE = "http://portal.ccu.edu.tw/ssoService.php?service=%s&linkId=%s";
    public static final String SSO_ECOURSE = "0000";
    public static final String SSO_SCORE = "0007";
    private static final String SSO_ECOURSE_URL = "http://ecourse.ccu.edu.tw/php/getssoCcuRight.php";
    private static final String SSO_SCORE_URL = "http://140.123.30.106/~ccmisp06/cgi-bin/library/SSO/Query_grade/getssoCcuRight.php";
    private static final String ERROR_WRONG_USERPASS = "錯誤代碼：LOGIN_001\\n帳號或密碼錯誤,請重新登錄！";
    private static final String ERROR_AUTOLOGOUT = "錯誤代碼：GLOBAL_001\\n您沒有權限，或是系統已自動登出，請重新登入！";
    private static final String ERROR_WORNG_AUTHCODE = "錯誤代碼：LOGIN_002\\n驗證碼錯誤,請重新登錄！";
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
        return getSession(sessionManager.getUserName(), sessionManager.getPassword());

    }

    public boolean getSession(String user, String pass) throws Exception {
        Connection connection;
        String location;
        Matcher matcher;
        try {
            connection = Jsoup.connect("http://portal.ccu.edu.tw/login_check.php");
            connection.data("acc", user)
                    .data("pass", pass);
            //.data("authcode", "請輸入右邊文字");
            connection.followRedirects(false);
            connection.post();

            location = connection.response().header("Location");
            if (location != null) {
                if(location.startsWith("http://portal.ccu.edu.tw/sso_index.php")) {

                    SESSIONID = connection.response().cookies().get("ccuSSO");
                    return SESSIONID != null;

                } else if(location.startsWith("http://portal.ccu.edu.tw/index.php")) {
                    matcher = Pattern.compile("alert\\(\"([^\"]+)\"\\);")
                            .matcher(
                                    connection.response().body()
                            );

                    if (matcher.find()) {
                        if (ERROR_WRONG_USERPASS.equals(matcher.group(1)))
                        {
                            throw new LoginErrorException("帳號或密碼錯誤");
                        } else if(ERROR_AUTOLOGOUT.equals(matcher.group(1))){
                            throw new LoginErrorException("請重試");
                        } else if(ERROR_WORNG_AUTHCODE.equals(matcher.group(1))) {
                            throw new LoginErrorException("認證碼錯誤!?!?!?");
                        } else {
                            throw new LoginErrorException("未辨識錯誤 : " + matcher.group(1));
                        }
                    }
                } else {
                    throw new LoginErrorException("學校系統更新 ?");
                }
            }

            SESSIONID = null;
            throw new LoginErrorException("未知錯誤");

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

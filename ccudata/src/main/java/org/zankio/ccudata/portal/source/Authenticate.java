package org.zankio.ccudata.portal.source;

import org.zankio.ccudata.base.exception.LoginErrorException;
import org.zankio.ccudata.base.model.AuthData;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.http.HTTPStringSource;
import org.zankio.ccudata.base.source.http.annotation.FollowRedirect;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.base.source.http.annotation.Url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Method("POST")
@Url("https://portal.ccu.edu.tw/login_check.php")
@FollowRedirect(false)

@DataType(Authenticate.TYPE)
public class Authenticate extends HTTPStringSource<AuthData, Boolean> {
    private static final String ERROR_WRONG_USERPASS = "錯誤代碼：LOGIN_001\\n帳號或密碼錯誤,請重新登錄！";
    private static final String ERROR_AUTOLOGOUT = "錯誤代碼：GLOBAL_001\\n您沒有權限，或是系統已自動登出，請重新登入！";
    private static final String ERROR_WORNG_AUTHCODE = "錯誤代碼：LOGIN_002\\n驗證碼錯誤,請重新登錄！";

    public final static String SSO_SESSION_ID = "SSO_SESSION_ID";
    public final static String TYPE = "AUTH";

    public static Request<Boolean, AuthData> request(String username, String password) {
        return new Request<>(TYPE, new AuthData(username, password), Boolean.class);
    }

    @Override
    public void initHTTPRequest(Request<Boolean, AuthData> request) {
        super.initHTTPRequest(request);
        AuthData args = request.args;
        httpParameter(request)
                .fields("acc", args.username)
                .fields("pass", args.password);
    }

    @Override
    protected Boolean parse(Request<Boolean, AuthData> request, HttpResponse response, String body) throws Exception {
        String location = response.header("Location");
        Matcher matcher;

        if (location != null) {
            if(location.startsWith("https://portal.ccu.edu.tw/sso_index.php")) {
                String cookie = response.cookie("ccuSSO");
                getContext().storage().put(SSO_SESSION_ID, cookie);
                return cookie != null;

            } else if(location.startsWith("https://portal.ccu.edu.tw/index.php")) {
                matcher = Pattern.compile("alert\\(\"([^\"]+)\"\\);").matcher(body);

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
        return false;
    }

}

package org.zankio.ccudata.ecourse.source.remote;

import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.http.HTTPSource;
import org.zankio.ccudata.base.source.http.annontation.Charset;
import org.zankio.ccudata.base.source.http.annontation.Field;
import org.zankio.ccudata.base.source.http.annontation.FollowRedirect;
import org.zankio.ccudata.base.source.http.annontation.Method;
import org.zankio.ccudata.base.source.http.annontation.Url;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.AuthData;

@Url(Urls.LOGIN)
@Method("POST")
@Charset("big5")
@Field({ "ver", "C" })
@FollowRedirect(true)

@DataType({Authenticate.TYPE})
public class Authenticate extends HTTPSource<AuthData, Boolean> {
    private static final String SESSION_FIELD_NAME = "PHPSESSID";
    private static final String ERROR_ID_PASS_WRONG = "帳號或密碼錯誤";
    public final static String TYPE = "AUTH";

    public static Request<Boolean, AuthData> request(String username, String password) {
        return new Request<>(TYPE, new AuthData(username, password), Boolean.class);
    }

    @Override
    public void initHTTPRequest(Request<Boolean, AuthData> request) {
        super.initHTTPRequest(request);
        AuthData args = request.args;
        httpParameter(request)
                .fields("id", args.username)
                .fields("pass", args.password);
    }

    @Override
    protected Boolean parse(Request request, HttpResponse response) throws Exception {
        String url = response.url();
        String body = response.string();

        if (url.startsWith("https://ecourse.ccu.edu.tw/php/Courses_Admin/take_course.php")) {
            EcourseSource.storageSession(getContext(), response.cookie(SESSION_FIELD_NAME));
            return true;
        } else if (url.startsWith(Urls.LOGIN)) {
            if (body != null && body.contains(ERROR_ID_PASS_WRONG))
                throw new Exception(ERROR_ID_PASS_WRONG);
        }
        throw new Exception("未知錯誤");
    }

}

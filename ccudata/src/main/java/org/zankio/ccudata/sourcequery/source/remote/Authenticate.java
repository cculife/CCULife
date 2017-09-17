package org.zankio.ccudata.sourcequery.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.constant.Exceptions;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.HTTPJsoupSource;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.base.source.http.annotation.Url;
import org.zankio.ccudata.base.model.AuthData;

@Method("POST")
@Url("https://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/Query/Query_grade.php")

@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@DataType(Authenticate.TYPE)
public class Authenticate extends HTTPJsoupSource<AuthData, Boolean> {
    public final static String TYPE = "AUTH";
    public final static String DOCUMENT_CACHE = "DOCUMENT_CACHE";

    public static Request<Boolean, AuthData> request(String username, String password) {
        return new Request<>(TYPE, new AuthData(username, password), Boolean.class);
    }

    @Override
    public void initHTTPRequest(Request<Boolean, AuthData> request) {
        super.initHTTPRequest(request);
        AuthData authData = request.args;
        httpParameter(request)
                .fields("id", authData.username)
                .fields("password", authData.password);
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
    protected Boolean parse(Request<Boolean, AuthData> request, HttpResponse response, Document document) throws Exception {
        String error;

        if (document.select("table").size() != 0) {
            cacheDocument(getContext(), document);
            return true;
        } else {
            error = parserError(document);

            if (error != null) {
                if (error.contains("學號應為九碼半形數字") ||
                        error.contains("請輸入正確學號") ||
                        error.contains("密碼長度過長或過短") ||
                        error.contains("您輸入的密碼有誤"))
                {
                    throw new Exception(Exceptions.ID_PASS_WRONG);
                } else {
                    throw new Exception("未知錯誤 : " + error);
                }
            }
            return false;
        }
    }

    public static void cacheDocument(Repository repository, Document document) {
        repository.storage().put(DOCUMENT_CACHE, document);
    }

    public static Document cacheDocument(Repository repository) {
        return repository.storage().get(DOCUMENT_CACHE, Document.class);
    }
}

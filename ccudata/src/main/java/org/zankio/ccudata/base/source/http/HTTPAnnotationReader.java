package org.zankio.ccudata.base.source.http;

import androidx.annotation.NonNull;

import org.zankio.ccudata.base.source.http.annotation.Cookie;
import org.zankio.ccudata.base.source.http.annotation.Field;
import org.zankio.ccudata.base.source.http.annotation.FollowRedirect;
import org.zankio.ccudata.base.source.http.annotation.Header;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.base.source.http.annotation.QueryString;
import org.zankio.ccudata.base.source.http.annotation.Url;

import static org.zankio.ccudata.base.utils.AnnotationUtils.getAnnotationValue;

public class HTTPAnnotationReader {
    public static HTTPParameter read(@NonNull HTTPSource target) {
        Class<? extends HTTPSource> targetClass = target.getClass();
        HTTPParameter parameter = new HTTPParameter();
        parameter.url(getAnnotationValue(targetClass, Url.class, ""));
        parameter.method(HTTPParameter.HTTPMethod.valueOf(getAnnotationValue(targetClass, Method.class, "GET")));

        //noinspection ConstantConditions
        parameter.followRedirect(getAnnotationValue(targetClass, FollowRedirect.class, Boolean.TRUE));

        String[] queryStrings = getAnnotationValue(targetClass, QueryString.class, new String[]{});
        for (int i = 0; i < (queryStrings != null ? queryStrings.length : 0); i += 2) {
            parameter.queryStrings(queryStrings[i], queryStrings[i + 1]);
        }

        String[] coolies = getAnnotationValue(targetClass, Cookie.class, new String[]{});
        for (int i = 0; i < (coolies != null ? coolies.length : 0); i += 2) {
            parameter.cookies(coolies[i], coolies[i + 1]);
        }

        String[] headers = getAnnotationValue(targetClass, Header.class, new String[]{});
        for (int i = 0; i < (headers != null ? headers.length : 0); i += 2) {
            parameter.headers(headers[i], headers[i + 1]);
        }

        String[] fields = getAnnotationValue(targetClass, Field.class, new String[]{});
        for (int i = 0; i < (fields != null ? fields.length : 0); i += 2) {
            parameter.fields(fields[i], fields[i + 1]);
        }

        return parameter;
    }

}

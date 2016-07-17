package org.zankio.ccudata.base.source.http;

import android.support.annotation.NonNull;

import org.zankio.ccudata.base.source.http.annontation.Cookie;
import org.zankio.ccudata.base.source.http.annontation.Field;
import org.zankio.ccudata.base.source.http.annontation.FollowRedirect;
import org.zankio.ccudata.base.source.http.annontation.Header;
import org.zankio.ccudata.base.source.http.annontation.Method;
import org.zankio.ccudata.base.source.http.annontation.QueryString;
import org.zankio.ccudata.base.source.http.annontation.Url;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public class HTTPAnnotationReader {
    public static HTTPParameter read(@NonNull HTTPSource target) {
        Class<? extends HTTPSource> targetClass = target.getClass();
        HTTPParameter parameter = new HTTPParameter();
        parameter.url(getValue(targetClass, Url.class, ""));
        parameter.method(HTTPParameter.HTTPMethod.valueOf(getValue(targetClass, Method.class, "GET")));

        //noinspection ConstantConditions
        parameter.followRedirect(getValue(targetClass, FollowRedirect.class, Boolean.TRUE));

        String[] queryStrings = getValue(targetClass, QueryString.class, new String[]{});
        for (int i = 0; i < (queryStrings != null ? queryStrings.length : 0); i += 2) {
            parameter.queryStrings(queryStrings[i], queryStrings[i + 1]);
        }

        String[] coolies = getValue(targetClass, Cookie.class, new String[]{});
        for (int i = 0; i < (coolies != null ? coolies.length : 0); i += 2) {
            parameter.cookies(coolies[i], coolies[i + 1]);
        }

        String[] headers = getValue(targetClass, Header.class, new String[]{});
        for (int i = 0; i < (headers != null ? headers.length : 0); i += 2) {
            parameter.headers(headers[i], headers[i + 1]);
        }

        String[] fields = getValue(targetClass, Field.class, new String[]{});
        for (int i = 0; i < (fields != null ? fields.length : 0); i += 2) {
            parameter.fields(fields[i], fields[i + 1]);
        }

        return parameter;
    }

    private static <A extends Annotation,T> T getValue(Class<?> target, Class<A> annontationClass, T defaultValue) {
        A annotation = target.getAnnotation(annontationClass);
        if (annotation == null) return defaultValue;
        else {
            //noinspection TryWithIdenticalCatches
            try {
                //noinspection unchecked
                return (T) annotation.getClass().getDeclaredMethod("value").invoke(annotation);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;

    }
}

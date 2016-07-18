package org.zankio.ccudata.base.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public class AnnotationUtils {
    public static <A extends Annotation,T> T getAnnotationValue(Class<?> target, Class<A> annontationClass, T defaultValue) {
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

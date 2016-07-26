package org.zankio.cculife.utils;

public class ExceptionUtils {
    public static Throwable extraceException(Throwable e) {
        if (e instanceof RuntimeException) {
            Throwable cause = e.getCause();
            if (cause != null)
                e = cause;
        }
        return e;
    }
}

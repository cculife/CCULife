package org.zankio.cculife.CCUService.Helper;

public final class Validate {

    private Validate() {}

    public static void isNull(Object object, String msg) {
        if(object == null)
            throw new IllegalArgumentException(msg);
    }
}

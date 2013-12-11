package org.zankio.cculife.override;

import org.jsoup.Connection;

public class Net{
    public final static int CONNECT_TIMEOUT = 10000;

    public static Connection connect(String url) {
        return org.jsoup.Jsoup.connect(url).timeout(CONNECT_TIMEOUT);
    }
}

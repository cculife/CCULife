package org.zankio.ccudata.ecourse.utils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParseUtils {
    public static Elements parseRow(Elements elements) {
        return elements.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");
    }

    public static Elements parseRow(Element elements) {
        return elements.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");
    }

    public static Elements parseField(Element elements) {
        return elements.select("td");
    }

    public static Elements parseField(Elements elements) {
        return elements.select("td");
    }


}

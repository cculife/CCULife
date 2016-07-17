package org.zankio.ccudata.base.source.http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.zankio.ccudata.base.model.HttpResponse;

public abstract class HTTPJsoupSource<TData> extends HTTPSource<TData>{

    @Override
    protected TData parse(HttpResponse body) throws Exception {
        return parse(body, Jsoup.parse(body.string()));
    }

    protected abstract TData parse(HttpResponse response, Document document);
}

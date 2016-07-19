package org.zankio.ccudata.base.source.http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;

public abstract class HTTPJsoupSource<TArgument, TData> extends HTTPSource<TArgument, TData>{

    @Override
    protected TData parse(Request<TData, TArgument> request, HttpResponse response) throws Exception {
        return parse(request, response, Jsoup.parse(response.string()));
    }

    protected abstract TData parse(Request<TData, TArgument> request, HttpResponse response, Document document) throws Exception;
}

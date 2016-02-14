package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;

public abstract class Arg1Source<TResult, TArg1> extends BaseSource<TResult> {
    public Arg1Source(BaseRepo context, SourceProperty property) {
        super(context, property);
    }

    @Override
    public TResult fetch(String type, Object... arg) throws Exception {
        if (arg.length < 1) throw new Exception("arg is miss");
        return _fetch(getArg1Class().cast(arg[0]));
    }

    protected abstract Class<TArg1> getArg1Class();
    public abstract TResult _fetch(TArg1 arg1) throws Exception;
    protected abstract String getUrl(TArg1 arg1);
    public abstract TResult parse(Document document, TArg1 arg1);
}

package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;

public abstract class Arg2Source<TResult, TArg1, TArg2> extends BaseSource<TResult> {
    public Arg2Source(BaseRepo context, SourceProperty property) {
        super(context, property);
    }

    @Override
    public TResult fetch(String type, Object... arg) throws Exception {
        if (arg.length < 2) throw new Exception("arg is miss");
        return _fetch(getArg1Class().cast(arg[0]), getArg2Class().cast(arg[1]));
    }

    protected abstract Class<TArg1> getArg1Class();
    protected abstract Class<TArg2> getArg2Class();
    protected abstract TResult _fetch(TArg1 arg1, TArg2 arg2) throws Exception;
    protected abstract String getUrl(TArg1 arg1, TArg2 arg2);
    public abstract TResult parse(Document document, TArg1 arg1, TArg2 arg2);
}

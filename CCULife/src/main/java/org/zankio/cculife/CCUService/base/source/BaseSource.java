package org.zankio.cculife.CCUService.base.source;

import org.zankio.cculife.CCUService.base.BaseRepo;

public abstract class BaseSource<T> implements ISource<T> {
    public final SourceProperty property;
    public final BaseRepo context;

    protected BaseSource(BaseRepo context, SourceProperty property) {
        this.property = property;
        this.context = context;
    }

    public void init() {
        if (property != null) {
            for (String type : property.DataList) {
                context.registerSource(this, type);
            }
        }
    }

    public abstract T fetch(String type, Object ...arg) throws Exception;
}


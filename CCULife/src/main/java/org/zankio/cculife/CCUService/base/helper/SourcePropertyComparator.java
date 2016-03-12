package org.zankio.cculife.CCUService.base.helper;

import org.zankio.cculife.CCUService.base.source.BaseSource;

import java.util.Comparator;

public class SourcePropertyComparator implements Comparator<BaseSource> {
    @Override
    public int compare(BaseSource l, BaseSource r) {
        return r.property.order.compareTo(l.property.order);
    }
}

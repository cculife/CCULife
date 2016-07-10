package org.zankio.ccudata.base.source;

import java.util.Comparator;

public class SourcePropertyComparator implements Comparator<BaseSource> {
    @Override
    public int compare(BaseSource l, BaseSource r) {
        return r.getOrder().compareTo(l.getOrder());
    }
}

package org.zankio.ccudata.base;

import org.zankio.ccudata.base.model.Request;

import java.util.Comparator;

public class RequestOrderComparator implements Comparator<Request> {
    @Override
    public int compare(Request l, Request r) {
        if (r == null) return -1;
        if (l == null) return 1;
        return r.source().getOrder().compareTo(l.source().getOrder());
    }
}

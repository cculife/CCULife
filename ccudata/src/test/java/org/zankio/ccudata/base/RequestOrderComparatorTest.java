package org.zankio.ccudata.base;

import junit.framework.Assert;

import org.junit.Test;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.base.source.SourceProperty;

public class RequestOrderComparatorTest {

    @Test
    public void testCompare() throws Exception {
        RequestOrderComparator comparator = new RequestOrderComparator();
        BaseSource low = new BaseSource() {
            @Override
            public SourceProperty.Level getOrder() {
                return SourceProperty.Level.LOW;
            }

            @Override
            public Object fetch(Request request) throws Exception { return null; }
        };

        BaseSource high = new BaseSource() {
            @Override
            public SourceProperty.Level getOrder() {
                return SourceProperty.Level.HIGH;
            }

            @Override
            public Object fetch(Request request) throws Exception { return null; }
        };


        Request requestLow = new Request().source(low);
        Request requestHigh = new Request().source(high);

        Assert.assertTrue(comparator.compare(requestHigh, requestLow) < 0);
        Assert.assertTrue(comparator.compare(requestHigh, requestHigh) == 0);
        Assert.assertTrue(comparator.compare( requestLow, requestHigh) > 0);
    }
}
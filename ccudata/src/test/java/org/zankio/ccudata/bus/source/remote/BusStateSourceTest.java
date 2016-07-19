package org.zankio.ccudata.bus.source.remote;

import org.junit.Test;
import org.zankio.ccudata.bus.model.BusStop;

public class BusStateSourceTest {

    @Test
    public void testFetch() throws Exception {
        BusStop[] fetch = new BusStateSource().fetch(BusStateSource.request("9018", "0", "1"));
        //TODO
    }
}
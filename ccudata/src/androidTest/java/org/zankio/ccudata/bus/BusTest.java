package org.zankio.ccudata.bus;

import org.junit.Test;
import org.zankio.ccudata.bus.model.BusStop;
import org.zankio.ccudata.bus.source.remote.BusStateSource;

public class BusTest {
    @Test
    public void testFetch() throws Exception {
        BusStop[] fetch = new Bus(null).fetch(BusStateSource.request("9018", "0", "1")).toBlocking().single().data();
    }
}
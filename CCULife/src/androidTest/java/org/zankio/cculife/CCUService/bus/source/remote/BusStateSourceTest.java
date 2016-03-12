package org.zankio.cculife.CCUService.bus.source.remote;

import android.test.AndroidTestCase;
import android.util.Log;

import org.junit.Test;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.bus.Bus;
import org.zankio.cculife.CCUService.bus.model.BusStop;

public class BusStateSourceTest extends AndroidTestCase{
    Bus bus = new Bus(getContext());
    BaseSource source = new BusStateSource(bus);

    @Test
    public void testFetch() throws Exception {
        // 7309 A 2;
        // 7309 0 2;

        // 7309 A 1
        // 7309 0 1
        BusStop[] fetch = (BusStop[]) source.fetch(BusStateSource.TYPE, "7309", "A", "1");
        for (BusStop bus : fetch) {
            Log.d("BusInfo", String.format("%s %s %s %s %s", bus.seq, bus.name, bus.id, bus.carNo, bus.perdiction));
        }
    }
}
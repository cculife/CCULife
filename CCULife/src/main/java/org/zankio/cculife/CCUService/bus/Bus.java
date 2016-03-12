package org.zankio.cculife.CCUService.bus;

import android.content.Context;

import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.bus.source.remote.BusStateSource;

public class Bus extends BaseRepo<Void> {
    public Bus(Context context) {
        super(context);
    }

    @Override
    protected BaseSource[] getSources() {
        return new BaseSource[]{
                new BusStateSource(this),
        };
    }
}

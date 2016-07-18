package org.zankio.ccudata.bus;

import android.content.Context;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.bus.source.remote.BusStateSource;

public class Bus extends Repository {
    public Bus(Context context) {
        super(context);
    }

    @Override
    protected BaseSource[] getSources() {
        return new BaseSource[]{
                new BusStateSource(),
        };
    }
}

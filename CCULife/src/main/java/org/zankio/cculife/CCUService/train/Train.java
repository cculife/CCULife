package org.zankio.cculife.CCUService.train;

import android.content.Context;

import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.train.source.remote.TrainStopStatusSource;

public class Train extends BaseRepo<Void>{
    public Train(Context context) {
        super(context);
    }

    @Override
    protected BaseSource[] getSources() {
        return new BaseSource[] {
                new TrainStopStatusSource(this)
        };
    }
}

package org.zankio.ccudata.train;

import android.content.Context;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.train.source.remote.PTXTrainTrainLineTypeSource;
import org.zankio.ccudata.train.source.remote.PTXTrainLiveDelaySource;
import org.zankio.ccudata.train.source.remote.PTXTrainStationTimetableSource;
import org.zankio.ccudata.train.source.remote.TrainStopStatusSource;


public class Train extends Repository{
    public Train(Context context) { super(context); }

    @Override
    protected BaseSource[] getSources() {
        return new BaseSource[] {
                new TrainStopStatusSource(),
                new PTXTrainLiveDelaySource(),
                new PTXTrainStationTimetableSource(),
                new PTXTrainTrainLineTypeSource()
        };
    }
}

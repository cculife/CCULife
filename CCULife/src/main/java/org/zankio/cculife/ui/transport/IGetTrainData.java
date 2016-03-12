package org.zankio.cculife.ui.transport;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.train.model.TrainTimetable;

public interface IGetTrainData {
    void getTrainStatus(String code, IOnUpdateListener<TrainTimetable> listener);
}

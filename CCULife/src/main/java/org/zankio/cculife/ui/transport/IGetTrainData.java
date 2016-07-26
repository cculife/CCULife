package org.zankio.cculife.ui.transport;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.train.model.TrainRequest;
import org.zankio.ccudata.train.model.TrainTimetable;

import rx.Observable;

public interface IGetTrainData {
    Observable<Response<TrainTimetable, TrainRequest>> getTrainStatus(String code);
}

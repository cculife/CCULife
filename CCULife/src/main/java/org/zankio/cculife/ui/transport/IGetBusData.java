package org.zankio.cculife.ui.transport;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.bus.model.BusLineRequest;
import org.zankio.ccudata.bus.model.BusStop;

import rx.Observable;

public interface IGetBusData {
    Observable<Response<BusStop[], BusLineRequest>> getBusState(String busNo, String branch, String isReturn);
}

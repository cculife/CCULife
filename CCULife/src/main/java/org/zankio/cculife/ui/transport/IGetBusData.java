package org.zankio.cculife.ui.transport;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.bus.model.BusStop;

public interface IGetBusData {
    void getBusState(String busNo, String branch, String isReturn, IOnUpdateListener<BusStop[]> listener);
}

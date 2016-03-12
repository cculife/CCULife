package org.zankio.cculife.ui.ecourse;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;

public interface IGetLoading {
    boolean isLoading();
    void setLoadedListener(IOnUpdateListener<Boolean> listener);
}

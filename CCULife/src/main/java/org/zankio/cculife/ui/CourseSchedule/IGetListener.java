package org.zankio.cculife.ui.CourseSchedule;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;

public interface IGetListener<T> {
    IOnUpdateListener<T> getUpdateListener();
    void registerListener(IOnUpdateListener<T> listener);
    void unregisterListener(IOnUpdateListener<T> listener);
}

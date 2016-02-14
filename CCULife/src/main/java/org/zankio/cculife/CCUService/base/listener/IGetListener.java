package org.zankio.cculife.CCUService.base.listener;

public interface IGetListener{
    IOnUpdateListener getListener(String type, Object... parameter);
}

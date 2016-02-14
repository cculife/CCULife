package org.zankio.cculife.CCUService.base.source;

public interface ISource<T> {
    T fetch(String type, Object ...arg) throws Exception;
}

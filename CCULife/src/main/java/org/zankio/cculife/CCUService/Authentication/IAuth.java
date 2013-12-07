package org.zankio.cculife.CCUService.Authentication;

public interface IAuth<T> {
    public T Auth(T connection);
}

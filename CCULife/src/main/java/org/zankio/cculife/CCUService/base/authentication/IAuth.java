package org.zankio.cculife.CCUService.base.authentication;

public interface IAuth<T> {
    T Auth(T connection);
}

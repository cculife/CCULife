package org.zankio.cculife.CCUService.base;

import org.jsoup.Connection;
import org.zankio.cculife.CCUService.base.authentication.IAuth;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BaseSession<T> {
    private T identity;
    private boolean authenticated = false;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private IAuth<Connection> auth;

    public BaseSession() { }
    public BaseSession(IAuth<Connection> auth) {
        setAuth(auth);
    }

    public Connection buildConnection(Connection connection){
        if (auth != null) return auth.Auth(connection);
        return connection;
    }


    public T getIdentity() {
        return this.identity;
    }
    public void setIdentity(T identity) throws Exception {
        this.identity = identity;
    }

    public ReadWriteLock getLock() {
        return this.lock;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public IAuth<Connection> getAuth() {
        return this.auth;
    }
    public void setAuth(IAuth<Connection> auth) {
        this.auth = auth;
    }
}

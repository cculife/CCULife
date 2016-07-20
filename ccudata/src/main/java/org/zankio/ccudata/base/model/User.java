package org.zankio.ccudata.base.model;

import android.support.annotation.NonNull;

import org.zankio.ccudata.base.Repository;

public class User {
    private Repository repository;
    private static final String USERNAME = "USER_USERNAME";
    private static final String PASSWORD = "USER_PASSWORD";

    public User(@NonNull Repository repository) {
        this.repository = repository;
    }

    public String username() {
        return repository.storage().get(USERNAME, String.class);
    }

    public User username(String username) {
        repository.storage().put(USERNAME, username);
        return this;
    }

    public String password() {
        return repository.storage().get(PASSWORD, String.class);
    }

    public User password(String password) {
        repository.storage().put(PASSWORD, password);
        return this;
    }
}

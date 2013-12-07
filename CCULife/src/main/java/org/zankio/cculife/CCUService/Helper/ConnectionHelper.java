package org.zankio.cculife.CCUService.Helper;

import org.jsoup.Connection;
import org.zankio.cculife.CCUService.Authentication.IAuth;
import org.zankio.cculife.override.Net;

public class ConnectionHelper {

    IAuth<Connection> auth;
    private final static int CONNECT_TIMEOUT = Net.CONNECT_TIMEOUT;

    public ConnectionHelper() { }

    public ConnectionHelper(IAuth<Connection> auth) {
        this.auth = auth;
    }

    public Connection initConnection(Connection connection) {
        ConnectionHelper.initAuth(connection, auth);
        ConnectionHelper.initTimeout(connection);
        return connection;
    }

    public static Connection initTimeout(Connection connection) {
        connection.timeout(CONNECT_TIMEOUT);
        return connection;
    }

    public static Connection initAuth(Connection connection, IAuth<Connection> auth) {
        if(auth != null) auth.Auth(connection);
        return connection;
    }
}

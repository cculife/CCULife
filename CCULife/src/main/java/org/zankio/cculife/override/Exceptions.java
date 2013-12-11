package org.zankio.cculife.override;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class Exceptions {
    public static String getNetworkErrorMessage(){
        return "網路無法使用";
    }

    public static String getLoginErrorMessage(){
        return "登入錯誤!!";
    }

    public static String getTimeoutMessage(){
        return "連線逾時";
    }

    public static String getNeedLogin(){
        return "請先登入";
    }

    public static Exception getNeedLoginException(){
        return new Exception(getNeedLogin());
    }

    public static Exception getNetworkException(){
        return new Exception(getNetworkErrorMessage());
    }

    public static Exception getNetworkException(Throwable throwable){
        if (throwable instanceof SocketTimeoutException)
            return new IOException(getTimeoutMessage(), throwable);
        else
            return new IOException(getNetworkErrorMessage(), throwable);
    }

    public static Exception getLoginErrorException(){
        return new Exception(getLoginErrorMessage());
    }

}

package org.zankio.cculife.override;


public class NetworkErrorException extends Exception {

    public NetworkErrorException(String detailMessage) {
        super(detailMessage);
    }

    public NetworkErrorException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NetworkErrorException(Throwable throwable) {
        super(throwable);
    }
}

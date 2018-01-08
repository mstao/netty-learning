package pers.mingshan.netty.production.common.exception;

public class ConnectFailedException extends RuntimeException {
    private static final long serialVersionUID = -5202369133955180312L;

    public ConnectFailedException() {
        super();
    }

    public ConnectFailedException(String message) {
        super(message);
    }

    public ConnectFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectFailedException(Throwable cause) {
        super(cause);
    }
}

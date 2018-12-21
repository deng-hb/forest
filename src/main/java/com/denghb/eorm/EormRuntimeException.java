package com.denghb.eorm;

public class EormRuntimeException extends RuntimeException {
    public EormRuntimeException() {
    }

    public EormRuntimeException(Throwable cause) {
        super(cause);
    }

    public EormRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}

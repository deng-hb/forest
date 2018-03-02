package com.denghb.forest;

/**
 * 异常
 */
public class ForestException extends RuntimeException {

    private int code;

    public ForestException(String message) {
        super(message);
    }

    public ForestException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ForestException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
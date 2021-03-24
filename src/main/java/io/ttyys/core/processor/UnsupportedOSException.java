package io.ttyys.core.processor;

/**
 * 不支持的操作系统异常
 */
public class UnsupportedOSException extends RuntimeException {

    public UnsupportedOSException(String message) {
        super(message);
    }

    public UnsupportedOSException(Throwable cause) {
        super(cause);
    }

    public UnsupportedOSException(String message, Throwable cause) {
        super(message, cause);
    }
}

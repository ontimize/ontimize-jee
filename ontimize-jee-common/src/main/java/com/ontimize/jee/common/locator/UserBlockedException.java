package com.ontimize.jee.common.locator;

public class UserBlockedException extends Exception {

    public UserBlockedException() {
        super();
    }

    public UserBlockedException(String message) {
        super(message);
    }

    public UserBlockedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserBlockedException(Throwable cause) {
        super(cause);
    }

}

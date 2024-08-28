package com.ericsson.itpf.deployment.test.operators;

public class LitpRestException extends RuntimeException {
    public LitpRestException(String message) {
        super(message);
    }

    public LitpRestException(String message, Throwable cause) {
        super(message, cause);
    }

}

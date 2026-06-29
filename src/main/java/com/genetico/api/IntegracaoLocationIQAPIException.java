package com.genetico.api;

public class IntegracaoLocationIQAPIException extends RuntimeException {
    public IntegracaoLocationIQAPIException(String message) {
        super(message);
    }

    public IntegracaoLocationIQAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}

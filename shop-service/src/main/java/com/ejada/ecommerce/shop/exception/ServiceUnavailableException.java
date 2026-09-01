package com.ejada.ecommerce.shop.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String service, Throwable cause) {
        super(service + " is unavailable", cause);
    }
}
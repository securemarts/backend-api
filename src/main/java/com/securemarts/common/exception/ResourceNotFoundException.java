package com.securemarts.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s not found for identifier: %s", resource, identifier));
    }
}

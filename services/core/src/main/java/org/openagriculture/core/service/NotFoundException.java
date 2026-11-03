package org.openagriculture.core.service;

/** A missing resource, including a resource owned by another tenant. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

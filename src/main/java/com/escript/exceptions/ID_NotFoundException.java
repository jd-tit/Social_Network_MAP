package com.escript.exceptions;

public class ID_NotFoundException extends RuntimeException {
    public ID_NotFoundException(String errorMessage) {
        super(errorMessage);
    }
}

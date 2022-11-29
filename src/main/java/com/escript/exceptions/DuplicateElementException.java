package com.escript.exceptions;

public class DuplicateElementException extends RuntimeException{
    public DuplicateElementException(String errorMessage){
        super(errorMessage);
    }
}

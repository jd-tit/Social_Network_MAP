package com.escript.exceptions.contextful;

public class FriendshipDoesNotExistException extends Exception{
    public FriendshipDoesNotExistException(String message) {
        super(message);
    }
    public FriendshipDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.escript.domain;

public class Password {
    private final char[] password;
    public Password(char[] password) {
        this.password = password;
    }

    public char[] get() {
        return password;
    }
}

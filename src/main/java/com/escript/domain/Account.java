package com.escript.domain;

public class Account extends Storable<Long> {
    private final User user;
    private final Password password;

    public Account(User user, String password) {
        this.password = new Password(password.toCharArray());
        this.user = user;
    }

    public Account(User user, Password password) {
        this.password = password;
        this.user = user;
    }

    public String getPasswordString() {
        return String.valueOf(password.get());
    }

    public char[] getPassword() {
        return password.get();
    }

    public User getUser() {
        return user;
    }
}

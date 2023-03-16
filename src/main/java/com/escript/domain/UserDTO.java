package com.escript.domain;

public class UserDTO extends Storable<Long> {
    private final User user;

    public User getUser() {
        return user;
    }

    public UserDTO(User user, Long id) {
        setIdentifier(id);
        this.user = user;
    }
}

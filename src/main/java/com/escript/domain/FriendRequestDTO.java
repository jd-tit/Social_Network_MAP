package com.escript.domain;

public class FriendRequestDTO {
    private final FriendRequest friendRequest;
    private final String usernameFrom;
    private final String usernameTo;

    public FriendRequestDTO(FriendRequest friendRequest, String usernameFrom, String usernameTo) {
        this.friendRequest = friendRequest;
        this.usernameFrom = usernameFrom;
        this.usernameTo = usernameTo;
    }

    public String getToWhom() {
        return usernameTo;
    }

    public String getFromWhom() {
        return usernameFrom;
    }

    public FriendRequest getRequest() {
        return friendRequest;
    }

    public String getUsernameFrom() {
        return usernameFrom;
    }

    public String getUsernameTo() {
        return usernameTo;
    }
}

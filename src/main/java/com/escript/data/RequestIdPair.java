package com.escript.data;

public class RequestIdPair {
    private final Long senderId;
    private final Long receiverId;

    public RequestIdPair(long senderId, long receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }
}

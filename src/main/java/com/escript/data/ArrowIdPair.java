package com.escript.data;

public class ArrowIdPair {
    private final Long senderId;
    private final Long receiverId;

    public ArrowIdPair(long senderId, long receiverId) {
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

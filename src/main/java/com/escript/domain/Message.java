package com.escript.domain;

import com.escript.data.ArrowIdPair;

import java.time.LocalDateTime;

public class Message extends Storable<Long> {
    private final String text;
    private final LocalDateTime dateSent;
    private final ArrowIdPair idPair;

    public String getText() {
        return text;
    }

    public LocalDateTime getDateSent() {
        return dateSent;
    }

    public ArrowIdPair getIdPair() {
        return idPair;
    }

    public Message(String text, ArrowIdPair idPair, LocalDateTime dateSent) {
        this.text = text;
        this.idPair = idPair;
        this.dateSent = dateSent;
    }
}

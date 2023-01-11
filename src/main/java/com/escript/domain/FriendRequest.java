package com.escript.domain;

import com.escript.data.RequestIdPair;

import java.io.IOError;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;

import static com.escript.domain.FriendRequest.State.*;

public class FriendRequest extends Storable<Long> {
    public enum State {PENDING, DENIED, ACCEPTED, RETRACTED}
    private final State state;
    private final RequestIdPair idPair;
    private final LocalDateTime dateSent;

    public FriendRequest(Long senderId, Long receiverId, LocalDateTime dateSent, State state)
    {
        this.state = state;
        this.idPair = new RequestIdPair(senderId, receiverId);
        this.dateSent = dateSent;
    }

    public static int getStateNumber(State state) {
        return switch (state) {
            case PENDING -> 1;
            case DENIED -> 2;
            case ACCEPTED -> 3;
            case RETRACTED -> 4;
        };
    }

    public static State getStateForNumber(int stateNumber) {
        return switch (stateNumber) {
            case 1 -> PENDING;
            case 2 -> DENIED;
            case 3 -> ACCEPTED;
            case 4 -> RETRACTED;
            default -> throw new InvalidParameterException("Invalid friend request state");
        };
    }

    public FriendRequest(Long senderId, Long receiverId, LocalDateTime dateSent, Integer state) {
        this.state = getStateForNumber(state);
        this.dateSent = dateSent;
        this.idPair = new RequestIdPair(senderId, receiverId);
    }

    public LocalDateTime getDateSent() {
        return dateSent;
    }

    public State getState() {
        return state;
    }

    public RequestIdPair getIdPair() {
        return idPair;
    }
}

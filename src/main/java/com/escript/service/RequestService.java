package com.escript.service;

import com.escript.data.IdPair;
import com.escript.data.RequestDbRepo;
import com.escript.data.RequestIdPair;
import com.escript.domain.Account;
import com.escript.domain.FriendRequest;
import com.escript.domain.FriendRequestDTO;
import com.escript.domain.User;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;
import com.escript.exceptions.contextful.FriendshipAlreadyRegisteredException;
import com.escript.exceptions.contextful.UserDoesNotExistException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOError;
import java.sql.SQLException;
import java.util.Collection;

public class RequestService {
    private final RequestDbRepo requestRepo;
    private final ObservableList<FriendRequestDTO> inboundRequests;

    private final ObservableList<FriendRequestDTO> inboundPendingRequests;
    private final ObservableList<FriendRequestDTO> outboundRequests;

    public RequestService() {
        requestRepo = new RequestDbRepo("Friend_requests");
        inboundRequests = FXCollections.observableArrayList();
        inboundPendingRequests = FXCollections.observableArrayList();
        outboundRequests = FXCollections.observableArrayList();
    }

    public void refreshRequestsReceivedByUser() {
        try {
            inboundRequests.setAll(
                    requestRepo.requestsReceivedBy(
                            GuiContext.getAccount().getIdentifier()
                    )
            );
        } catch (SQLException e) {
            throw new IOError(e);
        }

        inboundPendingRequests.setAll(
                inboundRequests.filtered(
                        x -> x.getRequest().getState() == FriendRequest.State.PENDING
                )
        );
    }

    public void refreshRequestsSentByUser() {
        try {
            outboundRequests.setAll(
                    requestRepo.requestsSentBy(GuiContext.getAccount().getIdentifier())
            );
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    public ObservableList<FriendRequestDTO> getInboundRequests() {
        return inboundRequests;
    }

    public ObservableList<FriendRequestDTO> getInboundPendingRequests() {
        return inboundPendingRequests;
    }

    public ObservableList<FriendRequestDTO> getOutboundRequests() {
        return outboundRequests;
    }

    public void sendRequest(String username) throws ID_NotFoundException, SQLException {
        var userService = GuiContext.getUserServiceProvider();
        Account target = userService.getAccount(username);
        requestRepo.addRequest(
                new RequestIdPair(
                        GuiContext.getAccount().getIdentifier(),
                        target.getIdentifier()
                ));
        refreshRequestsSentByUser();
    }

    public Collection<User> getPotentialFriends(String usernameFragment) {
        return requestRepo.getPotentialFriends(usernameFragment, GuiContext.getAccount().getIdentifier());
    }

    public void setRequestState(FriendRequest fr, FriendRequest.State state) throws SQLException {
        requestRepo.setRequestState(fr.getIdentifier(), state);
        try {
            if (state.equals(FriendRequest.State.ACCEPTED)) {
                GuiContext.getUserServiceProvider()
                        .markFriends(
                                new IdPair(fr.getIdPair().getSenderId(), fr.getIdPair().getReceiverId())
                        );
            }
        } catch (DuplicateElementException | UserDoesNotExistException | FriendshipAlreadyRegisteredException e) {
            throw new IOError(e);
        }
    }
}

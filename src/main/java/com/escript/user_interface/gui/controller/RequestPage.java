package com.escript.user_interface.gui.controller;

import com.escript.data.IdPair;
import com.escript.domain.FriendRequest;
import com.escript.domain.FriendRequestDTO;
import com.escript.domain.FriendshipDTO;
import com.escript.domain.User;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;
import com.escript.exceptions.contextful.FriendshipDoesNotExistException;
import com.escript.exceptions.contextful.UserDoesNotExistException;
import com.escript.service.GuiContext;
import com.escript.service.GuiService;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class RequestPage {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm");
    public TableView<FriendshipDTO> friendsView = new TableView<>();
    @FXML
    private TableView<FriendRequestDTO> sentRequests = new TableView<>();

    @FXML
    private TableView<FriendRequestDTO> receivedRequests = new TableView<>();
    @FXML
    private TextField searchBar;
    private final ObservableList<String> usernameResultList = FXCollections.observableArrayList();
    private final ObservableList<FriendshipDTO> friendList = FXCollections.observableArrayList();

    @FXML
    private ListView<String> userSelectorResults;

    private void setupColumns() {
        Function<FriendRequest, String> interpretRequestStatus =
        (FriendRequest fr) -> switch (fr.getState()) {
            case PENDING -> "Pending";
            case DENIED -> "Denied";
            case ACCEPTED -> "Accepted";
            case RETRACTED -> "Retracted";
        };

        Function<LocalDateTime, String> formatDate = dateFormatter::format;

        TableColumn<FriendRequestDTO, String> toWhom = new TableColumn<>("Name");
        TableColumn<FriendRequestDTO, String> fromWhom = new TableColumn<>("Name");
        TableColumn<FriendRequestDTO, String> outboundStatus = new TableColumn<>("Status");
        TableColumn<FriendRequestDTO, String> inboundRequestDates = new TableColumn<>("Date Sent");
        TableColumn<FriendRequestDTO, String> outboundRequestDates = new TableColumn<>("Date Sent");
        TableColumn<FriendshipDTO, String> friendName = new TableColumn<>("Name");
        TableColumn<FriendshipDTO, String> friendsSince = new TableColumn<>("Friends since");
        TableColumn<FriendshipDTO, Number> maxMessagesPerDay = new TableColumn<>("Best message combo");
        TableColumn<FriendshipDTO, Number> currentMessageForDay = new TableColumn<>("Current message combo");

        toWhom.setCellValueFactory(x -> new ReadOnlyStringWrapper(x.getValue().getToWhom()));
        fromWhom.setCellValueFactory(x -> new ReadOnlyStringWrapper(x.getValue().getFromWhom()));
        inboundRequestDates.setCellValueFactory(
                fr -> new ReadOnlyStringWrapper(
                        formatDate.apply(fr.getValue().getRequest().getDateSent()))
        );
        outboundRequestDates.setCellValueFactory(inboundRequestDates.getCellValueFactory());
        outboundStatus.setCellValueFactory(
                x -> new ReadOnlyStringWrapper(
                        interpretRequestStatus.apply(x.getValue().getRequest())
                )
        );
        friendName.setCellValueFactory(x -> new ReadOnlyStringWrapper(x.getValue().getFriendName()));
        friendsSince.setCellValueFactory(x -> new ReadOnlyStringWrapper(
                formatDate.apply(x.getValue().getFriendship().getFriendsSince()))
        );
        maxMessagesPerDay.setCellValueFactory(x -> new ReadOnlyIntegerWrapper(
                x.getValue().getFriendship().getLongestMessagingStreak())
        );
        currentMessageForDay.setCellValueFactory(x -> new ReadOnlyIntegerWrapper(
                x.getValue().getFriendship().getMessagingStreak())
        );

        sentRequests.getColumns().add(toWhom);
        sentRequests.getColumns().add(outboundRequestDates);
        sentRequests.getColumns().add(outboundStatus);

        receivedRequests.getColumns().add(fromWhom);
        receivedRequests.getColumns().add(inboundRequestDates);

        friendsView.getColumns().add(friendName);
        friendsView.getColumns().add(friendsSince);
        friendsView.getColumns().add(maxMessagesPerDay);
        friendsView.getColumns().add(currentMessageForDay);
    }

    private void setItemLists() {
        userSelectorResults.setItems(usernameResultList);
        sentRequests.setItems(
                GuiContext.getRequestServiceProvider().getOutboundRequests()
        );
        receivedRequests.setItems(
                GuiContext.getRequestServiceProvider().getInboundPendingRequests()
        );
        friendsView.setItems(friendList);
    }

    @FXML
    private void initialize() {
        setItemLists();
        setupColumns();

        receivedRequests.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        friendsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        sentRequests.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        updateSearchResults();
        updateFriendList();
        var requestService = GuiContext.getRequestServiceProvider();
        requestService.refreshRequestsSentByUser();
        requestService.refreshRequestsReceivedByUser();
    }

    public void updateSearchResults() {
        String usernameFragment = searchBar.getText();
        usernameResultList.setAll(
                GuiContext.getRequestServiceProvider()
                        .getPotentialFriends(usernameFragment)
                        .stream().map(User::getUsername)
                        .filter(x -> !x.equals(GuiContext.getAccount().getUser().getUsername()))
                        .toList()
        );
    }

    private void updateFriendList() {
        friendList.setAll(GuiContext
                .getUserServiceProvider()
                .friendshipsOf(
                        GuiContext.getAccount().getIdentifier())
        );
    }

    public void returnLink(ActionEvent actionEvent) throws IOException {
        GuiService.setView(GuiService.views.MAIN);
    }

    public void sendRequest(ActionEvent actionEvent) throws SQLException, ID_NotFoundException {
        var selected = userSelectorResults.getSelectionModel().getSelectedItems();
        if(selected.isEmpty()) {
            GuiService.showErrorMessage("You haven't selected an user");
            return;
        }
        String username = selected.get(0);
        var requestService = GuiContext.getRequestServiceProvider();
        requestService.sendRequest(username);
        updateSearchResults();
        requestService.refreshRequestsSentByUser();
    }

    public void refuseSelectedRequests(ActionEvent actionEvent) throws SQLException {
        var selected = receivedRequests.getSelectionModel().getSelectedItems();

        if (selected.isEmpty()) {
            GuiService.showErrorMessage("You haven't selected any users to refuse");
            return;
        }

        var requestService = GuiContext.getRequestServiceProvider();
        for (var request : selected) {
            requestService.setRequestState(request.getRequest(), FriendRequest.State.DENIED);
        }
        requestService.refreshRequestsReceivedByUser();
        updateFriendList();
    }

    public void acceptSelectedRequests(ActionEvent actionEvent) throws SQLException {
        var selected = receivedRequests.getSelectionModel().getSelectedItems();

        if (selected.isEmpty()) {
            GuiService.showErrorMessage("You haven't selected any users to accept");
            return;
        }

        var requestService = GuiContext.getRequestServiceProvider();
        for (var request : selected) {
            requestService.setRequestState(request.getRequest(), FriendRequest.State.ACCEPTED);
        }
        requestService.refreshRequestsReceivedByUser();
        updateFriendList();
    }

    public void unfriendSelected(ActionEvent actionEvent) throws DuplicateElementException, FriendshipDoesNotExistException, UserDoesNotExistException {
        var selected = friendsView.getSelectionModel().getSelectedItems();

        if (selected.isEmpty()) {
            GuiService.showErrorMessage("You haven't selected any friends to unfriend");
            return;
        }

        var userService = GuiContext.getUserServiceProvider();
        for (var friendshipDTO : selected) {
            var f = friendshipDTO.getFriendship();
            userService.unmarkFriends(
                    new IdPair(
                            f.getUserID1(),
                            f.getUserID2()
                    ));
        }
        updateFriendList();
    }

    public void cancelSelectedRequests(ActionEvent actionEvent) throws SQLException {
        var selected = sentRequests.getSelectionModel().getSelectedItems();

        if (selected.isEmpty()) {
            GuiService.showErrorMessage("You haven't selected any requests to cancel");
            return;
        }

        var requestService = GuiContext.getRequestServiceProvider();
        for (var request : selected) {
            requestService.setRequestState(request.getRequest(), FriendRequest.State.RETRACTED);
        }
        requestService.refreshRequestsSentByUser();
        updateSearchResults();
    }
}

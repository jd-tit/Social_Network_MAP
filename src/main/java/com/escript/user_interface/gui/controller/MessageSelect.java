package com.escript.user_interface.gui.controller;

import com.escript.domain.UserDTO;
import com.escript.service.GuiContext;
import com.escript.service.GuiService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.io.IOException;

public class MessageSelect {
    public TextField searchBar;
    public TableView<UserDTO> userSelectorResults;
    private final ObservableList<UserDTO> friendList = FXCollections.observableArrayList();

    public void initialize() {
        TableColumn<UserDTO, String> nameColumn = new TableColumn<>("Name");

        userSelectorResults.setItems(friendList);
        userSelectorResults.getColumns().add(nameColumn);
        nameColumn.setCellValueFactory(x -> new ReadOnlyStringWrapper(
                x.getValue().getUser().getUsername()
        ));
        updateSearchResults();
    }

    public void updateSearchResults() {
        var userService = GuiContext.getUserServiceProvider();
        var currentUser = GuiContext.getAccount();
        friendList.setAll(userService.friendsHavingUsernameLike(
                currentUser.getIdentifier(),
                searchBar.getText()
                ));
    }

    public void goToMessageView() throws IOException {
        var selected = userSelectorResults.getSelectionModel().getSelectedItems();
        if(selected.isEmpty()) {
            GuiService.showErrorMessage("You haven't selected someone to message.");
            return;
        }
        GuiContext.setMessageFriend(selected.get(0));
        GuiService.setView(GuiService.views.MESSAGE);
    }
}

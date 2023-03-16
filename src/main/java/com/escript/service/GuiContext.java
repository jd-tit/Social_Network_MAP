package com.escript.service;

import com.escript.domain.Account;
import com.escript.domain.UserDTO;
import javafx.beans.property.SimpleObjectProperty;

public class GuiContext {
    private static Account account;

    private static final SimpleObjectProperty<UserDTO> messageFriendProperty = new SimpleObjectProperty<>();
    private static UserService userService;
    private static RequestService requestService;
    private static MessageService messageService;

    public static void setAccount(Account account) {
        GuiContext.account = account;
    }

    public static Account getAccount() {
        return account;
    }

    public static UserService getUserServiceProvider() {
        return userService;
    }

    public static void setUserServiceProvider(UserService service) {
        GuiContext.userService = service;
    }

    public static RequestService getRequestServiceProvider() {
        return requestService;
    }

    public static void setRequestServiceProvider(RequestService service) {
        requestService = service;
    }

    public static void setMessageFriend(UserDTO messageFriend) {
        GuiContext.messageFriendProperty.setValue(messageFriend);
    }

    public static UserDTO getMessageFriend() {
        return messageFriendProperty.getValue();
    }

    public static SimpleObjectProperty<UserDTO> getMessageFriendProperty() {
        return messageFriendProperty;
    }

    public static void setMessageServiceProvider(MessageService service) {
        messageService = service;
    }

    public static MessageService getMessageServiceProvider() {
        return messageService;
    }
}

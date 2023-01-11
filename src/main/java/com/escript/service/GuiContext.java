package com.escript.service;

import com.escript.domain.Account;

public class GuiContext {
    private static Account account;
    private static UserService userService;
    private static RequestService requestService;

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
}

package com.escript.user_interface.gui.controller;

import com.escript.domain.Account;
import com.escript.domain.User;
import com.escript.exceptions.ID_NotFoundException;
import com.escript.service.GuiContext;
import com.escript.service.GuiService;
import com.escript.service.RequestService;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

public class Login {
    @FXML
    private TextField usernameInput;
    @FXML
    private PasswordField passwordInput;

    public void submitLogin() throws IOException, ID_NotFoundException {
        Account account = new Account(
                new User(usernameInput.getText()),
                passwordInput.getText()
        );
        if (GuiContext.getUserServiceProvider().authenticateAccount(account)){
            GuiContext.setAccount(
                    GuiContext.getUserServiceProvider().getAccount(account.getUser().getUsername())
            );
            GuiService.setView(GuiService.views.MAIN);
        } else {
            GuiService.showErrorMessage("The account details you have provided were not found in our database.");
        }
    }

    public void createAccount() throws IOException {
        GuiService.setView(GuiService.views.REGISTER);
    }
}

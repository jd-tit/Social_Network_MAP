package com.escript.user_interface.gui.controller;

import com.escript.domain.User;
import com.escript.exceptions.DuplicateElementException;
import com.escript.service.GuiContext;
import com.escript.service.GuiService;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.escript.domain.Account;

import java.io.IOException;
import java.sql.SQLException;

public class Register {
    public TextField usernameInput;
    public PasswordField passwordInput;
    public PasswordField confirmPasswordInput;

    public void login() throws IOException {
        GuiService.setView(GuiService.views.LOGIN);
    }

    public void createAccount() throws IOException, SQLException {
        if (!passwordInput.getText().equals(confirmPasswordInput.getText())) {
            GuiService.showErrorMessage("Passwords don't match.");
            return;
        }

        Account account = new Account(
                new User(usernameInput.getText()),
                passwordInput.getText()
        );

        try {
            GuiContext.getUserServiceProvider().addAccount(account);
        } catch (DuplicateElementException e) {
            GuiService.showErrorMessage("An account with this username already exists. Please try a different one.");
            usernameInput.clear();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Your account was created successfully. Please log in to continue.");
        alert.showAndWait();
        login();
    }
}

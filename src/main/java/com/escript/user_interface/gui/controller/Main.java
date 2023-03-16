package com.escript.user_interface.gui.controller;

import com.escript.service.GuiService;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import java.io.IOException;

public class Main {
    public void linkRequestPage(ActionEvent actionEvent) throws IOException {
        GuiService.setView(GuiService.views.REQUESTS);
    }

    public void quit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void messageSelectView(ActionEvent actionEvent) throws IOException {
        GuiService.setView(GuiService.views.MESSAGE_SELECT);
    }
}

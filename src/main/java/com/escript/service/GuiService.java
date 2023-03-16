package com.escript.service;

import com.escript.FXApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class GuiService {
    public enum views {MAIN, LOGIN, REGISTER, REQUESTS, MESSAGE_SELECT, MESSAGE}
    private static Stage stage;

    public static void init(Stage stage) {
        GuiService.stage = stage;
    }

    public static void init(Stage stage, views view) throws IOException {
        init(stage);
        setView(view);
        stage.show();
    }

    public static void setView(views view) throws IOException {

        String path = switch (view) {
            case MAIN -> "/fxml/MainView.fxml";
            case LOGIN -> "/fxml/LoginView.fxml";
            case REGISTER -> "/fxml/RegisterView.fxml";
            case REQUESTS -> "/fxml/RequestView.fxml";
            case MESSAGE -> "/fxml/MessageView.fxml";
            case MESSAGE_SELECT -> "/fxml/MessageSelectView.fxml";
        };

        FXMLLoader fxmlLoader = new FXMLLoader(
                FXApp.class.getResource(path)
        );
        stage.setScene(
                new Scene(fxmlLoader.load())
        );
    }

    public static void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

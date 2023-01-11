package com.escript;

import com.escript.exceptions.DuplicateElementException;
import com.escript.service.GuiContext;
import com.escript.service.GuiService;
import com.escript.service.RequestService;
import com.escript.service.UserService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class FXApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            GuiContext.setUserServiceProvider(new UserService());
            GuiContext.setRequestServiceProvider(new RequestService());
            GuiService.init(primaryStage, GuiService.views.LOGIN);
        } catch (IOException | RuntimeException | SQLException e) {
            GuiService.showErrorMessage(e.getMessage());
            Platform.exit();
        } catch (DuplicateElementException e) {
            GuiService.showErrorMessage(e.getMessage());
        }
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> exceptionHandler(exception));
    }

    @Override
    public void stop() throws Exception {
        GuiContext.getUserServiceProvider().closeConnections();
    }

    private void exceptionHandler(Throwable e) {
        GuiService.showErrorMessage(e.getMessage());
        e.printStackTrace();
        if(e instanceof IOException || e instanceof RuntimeException || e instanceof SQLException) {
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

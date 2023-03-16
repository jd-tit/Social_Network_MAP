package com.escript;

import com.escript.exceptions.DuplicateElementException;
import com.escript.service.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public class FXApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            GuiContext.setUserServiceProvider(new UserService());
            GuiContext.setRequestServiceProvider(new RequestService());
            GuiContext.setMessageServiceProvider(new MessageService());
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
        GuiContext.getMessageServiceProvider().stopAutoUpdate();
        GuiContext.getUserServiceProvider().closeConnections();
        GuiContext.getMessageServiceProvider().closeConnection();
        GuiContext.getRequestServiceProvider().closeConnection();
    }

    public static void exceptionHandler(Throwable e) {
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

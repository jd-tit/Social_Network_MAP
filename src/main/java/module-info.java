module Social.Network.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires de.mkammerer.argon2.nolibs;
//    requires com.google.gson;
//    requires spring.boot.autoconfigure;
//    requires spring.web;
//    requires spring.boot;

    exports com.escript to javafx.graphics;
    exports com.escript.exceptions;
    exports com.escript.data;
    exports com.escript.exceptions.contextful;
    exports com.escript.domain;
    exports com.escript.user_interface.gui.controller to javafx.fxml;
    opens com.escript.user_interface.gui.controller to javafx.fxml;
}
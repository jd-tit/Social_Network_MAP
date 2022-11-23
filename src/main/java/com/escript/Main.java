package com.escript;

import com.escript.user_interface.TextUI;
//import net.datafaker.Faker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws SQLException {
        var textUI =  TextUI.getInstance();

//        String url = "jdbc:postgresql://localhost/social_network_apm";
//        Properties props = new Properties();
//        props.setProperty("user", "soc_net_crud");
//        props.setProperty("password", "ga9vFdCNmvRZui");
//        Connection connection = DriverManager.getConnection(url, props);
//
//        Statement s = connection.createStatement();
//        var res = s.executeQuery("SELECT * FROM test1");
//        System.out.println(res.toString());

        textUI.displayMenu();

        while(true) {
            String input = textUI.prompt();
            textUI.parseInput(input);
        }
    }
}
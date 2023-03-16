package com.escript.data;

import java.io.IOError;
import java.util.Properties;
import java.sql.*;

public abstract class DbRepository {
    public final String uniqueViolationCode = "23505";
    protected final Connection connection;

    public DbRepository() {
        Properties props = new Properties();
        //TODO: Please move this to some kind of environment var
        props.setProperty("user", "soc_net_crud");
        props.setProperty("password", "ga9vFdCNmvRZui");
        props.setProperty("currentSchema", "social_network");

        String databaseURL = "jdbc:postgresql:social_network_apm";
        try {
            connection = DriverManager.getConnection(databaseURL, props);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }
}

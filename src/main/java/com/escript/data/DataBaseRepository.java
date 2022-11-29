package com.escript.data;

import com.escript.domain.Storable;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;

import java.io.IOError;
import java.io.IOException;
import java.util.Properties;
import java.sql.*;
import java.util.function.Function;

public abstract class DataBaseRepository<E extends Storable<Long>> implements Repository<Long, E>{
    protected final String tableName;
    protected final Connection databaseConnection;

    public final int uniqueViolationCode = 23505;

    public DataBaseRepository(String tableName) {
        this.tableName = tableName;
        Properties props = new Properties();
        //TODO: Please move this to some kind of environment var
        props.setProperty("user", "soc_net_crud");
        props.setProperty("password", "ga9vFdCNmvRZui");
        props.setProperty("currentSchema", "social_network_apm");

        String databaseURL = "jdbc:postgresql:social_network_apm";
        try {
            databaseConnection = DriverManager.getConnection(databaseURL, props);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }
}

package com.escript.data;

import com.escript.domain.Friendship;
import com.escript.domain.User;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class UserDBRepo extends DataBaseRepository<User>{
    public UserDBRepo(String tableName) {
        super(tableName);
    }

    @Override
    public void add(User user) throws DuplicateElementException {
        String insertCommand = "INSERT INTO social_network_apm.\"Users\" (username) values (?)";
        try {
            PreparedStatement statement = this.databaseConnection.prepareStatement(insertCommand);
            statement.setString(1, user.getUsername());
            statement.executeUpdate();
        } catch (SQLException e) {
            if(e.getErrorCode() == this.uniqueViolationCode)
                throw new DuplicateElementException("");

            e.printStackTrace();
            throw new IOError(e);
        }
    }

    @Override
    public void addAll(Iterable<User> users) throws DuplicateElementException {
        for(var user : users)
            this.add(user);
    }

    @Override
    public Collection<User> getAll() {
        String selectCommand = "SELECT username, id FROM social_network_apm.\"Users\"";
        ResultSet rs;
        try {
            Statement statement = this.databaseConnection.createStatement();
            rs = statement.executeQuery(selectCommand);

            ArrayList<User> result = new ArrayList<>();
            while (rs.next()) {
                User user = new User(rs.getString("username"));
                user.setIdentifier(rs.getLong("id"));
                result.add(user);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    @Override
    public User get(Long id) throws ID_NotFoundException {
        String selectCommand = "SELECT username, id FROM social_network_apm.\"Users\" WHERE id = " + id.toString();
        ResultSet rs;
        try {
            Statement statement = this.databaseConnection.createStatement();
            rs = statement.executeQuery(selectCommand);
            if (!rs.next()) {
                throw new ID_NotFoundException("");
            }

            User result = new User(rs.getString("username"));
            result.setIdentifier(rs.getLong("id"));
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public User getByName(String name) throws ID_NotFoundException {
        String selectCommand = "SELECT username, id FROM social_network_apm.\"Users\"" +
                " WHERE username = ?";
        ResultSet rs;
        try {
            PreparedStatement statement = this.databaseConnection.prepareStatement(selectCommand);
            statement.setString(1, name);
            rs = statement.executeQuery();
            if (!rs.next()) {
                throw new ID_NotFoundException("");
            }

            User result = new User(rs.getString("username"));
            result.setIdentifier(rs.getLong("id"));
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    @Override
    public void remove(Long id) throws ID_NotFoundException {
        String deleteCommand = "DELETE FROM social_network_apm.\"Users\" WHERE id = " + id.toString();
        try {
            PreparedStatement statement = this.databaseConnection.prepareStatement(deleteCommand);
            int affected = statement.executeUpdate();
            if(affected < 1)
                throw new ID_NotFoundException("");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    @Override
    public void removeAll() {
        String truncateCommand = "TRUNCATE TABLE ONLY social_network_apm.\"Users\" RESTART IDENTITY";
        try {
            Statement statement = this.databaseConnection.createStatement();
            statement.executeUpdate(truncateCommand);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    private User extractUser(ResultSet rs) throws SQLException {
        return new User(rs.getString("username"));
    }
}

package com.escript.data;

import com.escript.domain.Account;
import com.escript.domain.User;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;

import java.io.IOError;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class AccountDBRepo extends DbRepository {
    public AccountDBRepo() {
        super();
    }

    public long add(Account account) throws DuplicateElementException, SQLException {
        String cmd = "INSERT INTO sn.users (username, password) values (?, ?)";

        try (var s = connection.prepareStatement(cmd, Statement.RETURN_GENERATED_KEYS))
        {
            s.setString(1, account.getUser().getUsername());
            s.setString(2, account.getPasswordString());
            s.executeUpdate();
            try (ResultSet keySet = s.getGeneratedKeys()) {
                keySet.next();
                return keySet.getLong(1);
            }

        } catch (SQLException e) {
            if(e.getSQLState().equals(this.uniqueViolationCode))
                throw new DuplicateElementException("Account already exists in database");
            throw e;
        }
    }


    public Collection<Account> getAll() {
        String cmd = "SELECT id, username, password FROM sn.users";

        try (var s = this.connection.createStatement()) {
            ArrayList<Account> result = new ArrayList<>();
            try (var rs = s.executeQuery(cmd)) {
                while (rs.next())
                    result.add(extractAccount(rs));
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public Account get(Long accountId) throws ID_NotFoundException {
        String cmd = "SELECT id, username, password FROM sn.users WHERE id = ?";
        try (var s = connection.prepareStatement(cmd)) {
            s.setLong(1, accountId);
            try (var  rs = s.executeQuery()) {
                if (rs.isBeforeFirst()) {
                    rs.next();
                    return extractAccount(rs);
                }
                String message = String.format("Couldn't find any accounts with ID %d.", accountId);
                throw new ID_NotFoundException(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public Account get(String username) throws ID_NotFoundException {
        String cmd = "SELECT id, username, password FROM sn.users WHERE username = ?";
        try (var s = connection.prepareStatement(cmd)) {
            s.setString(1, username);
            try (var rs = s.executeQuery()) {
                if (rs.isBeforeFirst()) {
                    rs.next();
                    return extractAccount(rs);
                }
                String message = String.format("Couldn't find any account having the username '%s'.", username);
                throw new ID_NotFoundException(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public void remove(Long accountId) throws ID_NotFoundException, SQLException {
        String cmd = "DELETE FROM sn.users WHERE id = ?";
        try (var s = connection.prepareStatement(cmd)) {
            s.setLong(1, accountId);
            int removed = s.executeUpdate();
            if (removed == 0) {
                String message = String.format(
                        "Couldn't remove account with ID %d because it does not exist", accountId);
                throw new ID_NotFoundException(message);
            }
        }
    }

    public void remove(String username) throws ID_NotFoundException, SQLException {
        String cmd = "DELETE FROM sn.users WHERE username = ?";
        try (var s = connection.prepareStatement(cmd)) {
            s.setString(1, username);
            int removed = s.executeUpdate();
            if (removed == 0) {
                String message = String.format(
                        "Couldn't remove account with username '%s' because it does not exist", username);
                throw new ID_NotFoundException(message);
            }
        }
    }

    public static User extractUser(ResultSet rs) throws  SQLException {
        return new User(rs.getString("username"));
    }
    public static Account extractAccount(ResultSet rs) throws SQLException {
        var account =  new Account(
                extractUser(rs),
                rs.getString("password")
        );
        account.setIdentifier(rs.getLong("id"));
        return account;
    }
}

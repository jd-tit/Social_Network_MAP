package com.escript.data;

import com.escript.domain.Friendship;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;

import java.io.IOError;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class FriendshipDBRepo extends DataBaseRepository<Friendship>{
    //TODO: Use the `tableName` in queries
    public FriendshipDBRepo(String tableName) {
        super(tableName);
    }

    @Override
    public void add(Friendship friendship) throws DuplicateElementException {
        String insertInFriendships = "INSERT INTO social_network_apm.\"Friendships\" " +
                "(since, currentmessagestreak, bestmessagestreak, user_id1, user_id2) " +
                "values (?, ?, ?, ? ,?)";
        try {
            PreparedStatement friendStatement = this.databaseConnection.prepareStatement(insertInFriendships);
            friendStatement.setTimestamp(1, Timestamp.valueOf(friendship.getFriendsSince()));
            friendStatement.setInt(2, friendship.getMessagingStreak());
            friendStatement.setInt(3, friendship.getLongestMessagingStreak());
            friendStatement.setLong(4, friendship.getUserID1());
            friendStatement.setLong(5, friendship.getUserID2());

            friendStatement.executeUpdate();
        } catch (SQLException e) {
            if(e.getErrorCode() == this.uniqueViolationCode)
                throw new DuplicateElementException("");

            e.printStackTrace();
            throw new IOError(e);
        }
    }

    @Override
    public void addAll(Iterable<Friendship> friendships) throws DuplicateElementException {
        for(var friendship : friendships)
            this.add(friendship);
    }

    @Override
    public Collection<Friendship> getAll() {
        String selectCommand = "SELECT user_id1, user_id2, friendship_id, since, currentmessagestreak, bestmessagestreak" +
                " FROM social_network_apm.\"Friendships\"" +
                " ORDER BY friendship_id";
        ResultSet rs;
        try {
            Statement statement = this.databaseConnection.createStatement();
            rs = statement.executeQuery(selectCommand);

            ArrayList<Friendship> result = new ArrayList<>();
            while (rs.next()) {
                LocalDateTime friendsSince = rs.getTimestamp("since").toLocalDateTime();
                Friendship friendship = new Friendship(
                        rs.getLong("user_id1"),
                        rs.getLong("user_id2"),
                        friendsSince
                );
                friendship.setIdentifier(rs.getLong("friendship_id"));
                friendship.setMessagingStreak(rs.getInt("currentmessagestreak"));
                friendship.setLongestMessagingStreak(rs.getInt("bestmessagestreak"));
                result.add(friendship);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    @Override
    public Friendship get(Long id) throws ID_NotFoundException {
        String selectCommand = "SELECT user_id1, user_id2, friendship_id, since, currentmessagestreak, bestmessagestreak" +
                " FROM social_network_apm.\"Friendships\"" +
                " WHERE friendship_id = " + id.toString();

        try {
            Statement statement = this.databaseConnection.createStatement();
            ResultSet rs = statement.executeQuery(selectCommand);
            if (!rs.next()) {
                throw new ID_NotFoundException("");
            }

            return extractFriendship(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public Friendship getByMembers(Long id1, Long id2) throws ID_NotFoundException {
        String selectCommand = "SELECT user_id1, user_id2, friendship_id, since, currentmessagestreak, bestmessagestreak" +
                " FROM social_network_apm.\"Friendships\"" +
                " WHERE user_id1 = ?" +
                " AND user_id2 = ?" +
                " ORDER BY friendship_id";
        
        try {
            PreparedStatement statement = this.databaseConnection.prepareStatement(selectCommand);
            statement.setLong(1, id1);
            statement.setLong(2, id2);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) {
                throw new ID_NotFoundException("");
            }

            return extractFriendship(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    @Override
    public void remove(Long friendshipId) throws ID_NotFoundException {
        String deleteCommand = "DELETE FROM social_network_apm.\"Friendships\"" +
                " WHERE friendship_id = ?";

        try {
            PreparedStatement statement = this.databaseConnection.prepareStatement(deleteCommand);
            statement.setLong(1, friendshipId);
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
        String truncateCommand = "TRUNCATE TABLE ONLY social_network_apm.\"Friendships\" RESTART IDENTITY";
        try {
            Statement statement = this.databaseConnection.createStatement();
            statement.executeUpdate(truncateCommand);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public void removeByMembers(Long id1, Long id2) throws ID_NotFoundException {
        String deleteCommand = "DELETE FROM social_network_apm.\"Friendships\"" +
                " WHERE user_id1 = ? AND user_id2 = ?";
        try {
            PreparedStatement statement = this.databaseConnection.prepareStatement(deleteCommand);
            statement.setLong(1, id1);
            statement.setLong(2, id2);
            int affected = statement.executeUpdate();
            if(affected < 1)
                throw new ID_NotFoundException("");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public Iterable<Friendship> friendshipsOf(Long id) {
        String selectCommand =
                "SELECT user_id1, user_id2, friendship_id, since, currentmessagestreak, bestmessagestreak" +
                " FROM social_network_apm.\"Friendships\"" +
                " WHERE user_id1 = ? OR user_id2 = ?" +
                " ORDER BY friendship_id";
        ResultSet rs;
        ArrayList<Friendship> result=  new ArrayList<>();
        try {
            PreparedStatement statement = this.databaseConnection.prepareStatement(selectCommand);
            statement.setLong(1, id);
            statement.setLong(2, id);
            rs = statement.executeQuery();

            while (rs.next())
                result.add(extractFriendship(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
        return result;
    }

    public void update(Friendship fr) throws ID_NotFoundException {
        String selectCommand = "UPDATE social_network_apm.\"Friendships\"" +
                " SET since = ?, currentmessagestreak = ?, bestmessagestreak = ?" +
                " WHERE friendship_id = ?";

        try {
            PreparedStatement statement = this.databaseConnection.prepareStatement(selectCommand);
            statement.setTimestamp(1, Timestamp.valueOf(fr.getFriendsSince()));
            statement.setInt(2, fr.getMessagingStreak());
            statement.setInt(3, fr.getLongestMessagingStreak());
            statement.setLong(4, fr.getIdentifier());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    private Friendship extractFriendship(ResultSet rs) throws SQLException {
        return new Friendship(
                rs.getLong("friendship_id"),
                rs.getLong("user_id1"),
                rs.getLong("user_id2"),
                rs.getTimestamp("since").toLocalDateTime(),
                rs.getInt("currentmessagestreak"),
                rs.getInt("bestmessagestreak")
        );
    }
}

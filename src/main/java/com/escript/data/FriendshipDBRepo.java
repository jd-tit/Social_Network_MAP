package com.escript.data;

import com.escript.domain.Friendship;
import com.escript.domain.FriendshipDTO;
import com.escript.domain.User;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;
import com.escript.exceptions.contextful.FriendshipDoesNotExistException;

import java.io.IOError;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class FriendshipDBRepo extends DbRepository {
    //TODO: Use the `tableName` in queries

    public FriendshipDBRepo(String tableName) {
        super(tableName);
    }

    public void add(Friendship f) throws SQLException, DuplicateElementException {
        String addFriendshipCmd = "INSERT INTO sn.friendships " +
                "(since, current_message_streak, best_message_streak) " +
                "VALUES (CURRENT_TIMESTAMP, 0, 0)";
        String linkUsersCmd = "INSERT INTO sn.friends (user_id, friendship_id) VALUES " +
                "(?, ?), (?, ?)";

        try (
                var addFriendship = connection.prepareStatement(addFriendshipCmd, Statement.RETURN_GENERATED_KEYS);
                var linkUsers = connection.prepareStatement(linkUsersCmd)
        ) {
            addFriendship.executeUpdate();
            try (var keySet = addFriendship.getGeneratedKeys()) {
                keySet.next();
                long friendshipId = keySet.getLong(1);

                linkUsers.setLong(1, f.getUserID1());
                linkUsers.setLong(2, friendshipId);
                linkUsers.setLong(3, f.getUserID2());
                linkUsers.setLong(4, friendshipId);
                linkUsers.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals(uniqueViolationCode)) {
                String message = String.format(
                        "An account with ID %d already exists in the database", f.getIdentifier());
                throw new DuplicateElementException(message);
            }
            throw e;
        }
    }

    public Collection<IdPair> getIdPairs() throws SQLException {
        String selectCommand =
                "select fs.friendship_id, f1.user_id AS user_id1, f2.user_id AS user_id2 from sn.friendships AS fs " +
                "join sn.friends AS f1 ON fs.friendship_id = f1.friendship_id " +
                "join sn.friends as f2 ON fs.friendship_id = f2.friendship_id " +
                "WHERE f1.user_id < f2.user_id " +
                "ORDER BY fs.friendship_id";

        try (var statement = this.connection.createStatement()) {
            try (var rs = statement.executeQuery(selectCommand)) {
                ArrayList<IdPair> idPairs = new ArrayList<>();
                while (rs.next()) {
                    long user_id1 = rs.getLong("user_id1");
                    long user_id2 = rs.getLong("user_id2");
                    try {
                        idPairs.add(new IdPair(user_id1, user_id2));
                    } catch (DuplicateElementException e) {
                        throw new IOError(e);
                    }
                }
                return idPairs;
            }
        }
    }


    public Friendship get(IdPair idPair) throws ID_NotFoundException {
        String selectCommand =
            "select fs.friendship_id as friendship_id, f1.user_id AS user_id1, f2.user_id AS user_id2, " +
            "fs.current_message_streak AS current_combo, fs.best_message_streak as best_combo," +
            "fs.since AS since " +
            "from sn.friendships AS fs " +
            "join sn.friends AS f1 ON fs.friendship_id = f1.friendship_id " +
            "join sn.friends as f2 ON fs.friendship_id = f2.friendship_id " +
            "WHERE f1.user_id = ? AND f2.user_id = ? " +
            "ORDER BY fs.friendship_id";

//                "SELECT user_id1, user_id2, friendship_id, since, current_message_streak, best_message_streak " +
//                "FROM sn.friendships AS F " +
//                "RIGHT JOIN friends AS L ON F.friendship_id = L.friendship_id " +
//                "WHERE user_id1 = ? " +
//                "AND user_id2 = ? " +
//                "ORDER BY friendship_id";
        
        try (var statement = this.connection.prepareStatement(selectCommand)) {
            statement.setLong(1, idPair.getFirst());
            statement.setLong(2, idPair.getSecond());
            try (var rs = statement.executeQuery()) {
                if (rs.isBeforeFirst()) {
                    rs.next();
                    return extractFriendship(rs);
                }
                String message = String.format(
                        "Couldn't get friendship of users with IDs %d and %d, because it doesn't exist.",
                        idPair.getFirst(),
                        idPair.getSecond());
                throw new ID_NotFoundException(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public void remove(Long friendshipId) throws ID_NotFoundException {
        String cmd = "DELETE FROM sn.friendships WHERE friendship_id = ?";
        try (var s = connection.prepareStatement(cmd)){
            s.setLong(1, friendshipId);
            int removed = s.executeUpdate();
            if (removed == 0) {
                String message = String.format("Couldn't remove friendship with inexistent ID %d", friendshipId);
                throw new ID_NotFoundException(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public void removeAll() {
        String cmd = "TRUNCATE TABLE ONLY sn.friendships RESTART IDENTITY";
        try (var s = connection.createStatement()) {
            s.executeUpdate(cmd);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public void remove(IdPair idPair) throws ID_NotFoundException {
        String cmd = "DELETE FROM sn.friendships " +
                "WHERE friendship_id IN " +
                "(SELECT fs.friendship_id FROM sn.friendships fs " +
                "JOIN sn.friends f1 ON fs.friendship_id = f1.friendship_id " +
                "JOIN sn.friends f2 ON fs.friendship_id = f2.friendship_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?)";
        try (var s = this.connection.prepareStatement(cmd)){
            s.setLong(1, idPair.getFirst());
            s.setLong(2, idPair.getSecond());
            int affected = s.executeUpdate();
            if(affected < 1)
                throw new ID_NotFoundException("");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public Collection<FriendshipDTO> friendshipsOf(Long accountId) {
        String cmd = "SELECT U2.username AS friend_username, F.user_id AS user_id1, " +
                "F2.user_id AS user_id2, FS.friendship_id AS friendship_id, " +
                "since, current_message_streak AS current_combo, best_message_streak AS best_combo " +
                "FROM sn.users AS U " +
                "JOIN sn.friends F ON U.id = F.user_id " +
                "JOIN sn.friendships FS ON F.friendship_id = FS.friendship_id " +
                "JOIN sn.friends F2 ON F2.friendship_id = FS.friendship_id " +
                "JOIN sn.users U2 ON U2.id = F2.user_id " +
                "WHERE U.id = ? AND U.id <> U2.id";
        ArrayList<FriendshipDTO> result=  new ArrayList<>();
        try (var s = this.connection.prepareStatement(cmd)) {
            s.setLong(1, accountId);

            try (var rs = s.executeQuery()) {
                while (rs.next())
                    result.add(extractFriendshipDTO(rs));
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public Collection<User> friendsOf(String username) {
        String cmd = "SELECT U2.username AS username FROM sn.users U" +
                " JOIN sn.friends F ON U.id = F.user_id " +
                " JOIN sn.users U2 ON U2.id = F.friendship_id " +
                " WHERE U.username = ? AND U.id <> U2.id " +
                " ORDER BY U2.id";
        ArrayList<User> result = new ArrayList<>();
        try (var s = connection.prepareStatement(cmd)) {
            s.setString(1, username);
            try (var rs = s.executeQuery()) {
                while (rs.next())
                    result.add(
                            new User(rs.getString("username")));
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public void resetAllStreaks() throws SQLException {
        String cmd = "UPDATE sn.friendships SET current_message_streak = 0";
        try (var s = connection.prepareStatement(cmd)) {
            s.executeUpdate();
        }
    }

    public void incrementStreak(IdPair idPair) throws SQLException, FriendshipDoesNotExistException {
        String cmd = "UPDATE sn.friendships SET current_message_streak = current_message_streak + 1 " +
                "WHERE friendship_id IN " +
                "(SELECT fs.friendship_id FROM friendships fs " +
                "JOIN friends f1 on fs.friendship_id = f1.friendship_id " +
                "JOIN friends f2 ON fs.friendship_id = f2.friendship_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?)";
        try (PreparedStatement s = connection.prepareStatement(cmd)) {
            s.setLong(1, idPair.getFirst());
            s.setLong(2, idPair.getSecond());
            int updated = s.executeUpdate();
            if (updated == 0) {
                String msg = String.format(
                        "Couldn't increment streak for inexistent friendship between users with IDs %d and %d",
                        idPair.getFirst(), idPair.getSecond());
                throw new FriendshipDoesNotExistException(msg);
            }
        }
    }

    public static Friendship extractFriendship(ResultSet rs) throws SQLException {
        return new Friendship(
                rs.getLong("friendship_id"),
                rs.getLong("user_id1"),
                rs.getLong("user_id2"),
                rs.getTimestamp("since").toLocalDateTime(),
                rs.getInt("current_combo"),
                rs.getInt("best_combo")
        );
    }

    public static FriendshipDTO extractFriendshipDTO(ResultSet rs) throws SQLException {
        return new FriendshipDTO(
                extractFriendship(rs),
                rs.getString("friend_username")
                );
    }
}

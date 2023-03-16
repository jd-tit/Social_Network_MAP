package com.escript.data;

import com.escript.domain.FriendRequest;
import com.escript.domain.FriendRequestDTO;
import com.escript.domain.User;

import java.io.IOError;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static com.escript.data.AccountDBRepo.extractUser;

public class RequestDbRepo extends DbRepository {
    public RequestDbRepo(String tableName) {
        super(tableName);
    }

    public Collection<FriendRequestDTO> requestsSentBy(String username) throws SQLException {
        String cmd =
                "SELECT RU.username AS receiver_username, SU.username AS sender_username, " +
                "sender_id, receiver_id, sent, state, FR.id as id " +
                "FROM sn.friend_requests AS FR " +
                "JOIN sn.users AS RU on RU.id = FR.receiver_id " +
                "JOIN sn.users AS SU on SU.id = FR.sender_id " +
                "WHERE SU.username = ?";
        try (var s = connection.prepareStatement(cmd)) {
            s.setString(1, username);
            ArrayList<FriendRequestDTO> result = new ArrayList<>();
            try (var rs = s.executeQuery()) {
                while(rs.next()) {
                    result.add(extractFriendRequestDTO(rs));
                }
                return result;
            }
        }
    }

    public Collection<FriendRequestDTO> requestsSentBy(Long accountId) throws SQLException {
        String cmd =
                "SELECT RU.username AS receiver_username, SU.username AS sender_username, " +
                "sender_id, receiver_id, sent, state, FR.id as id " +
                "FROM sn.friend_requests AS FR " +
                "JOIN sn.users AS RU on RU.id = FR.receiver_id " +
                "JOIN sn.users AS SU on SU.id = FR.sender_id " +
                "WHERE sender_id = ?";
        try (var s = connection.prepareStatement(cmd)) {
            s.setLong(1, accountId);
            ArrayList<FriendRequestDTO> result = new ArrayList<>();
            try (var rs = s.executeQuery()) {
                while(rs.next()) {
                    result.add(extractFriendRequestDTO(rs));
                }
                return result;
            }
        }
    }

    public Collection<FriendRequestDTO> requestsReceivedBy(String username) throws SQLException {
        String cmd =
                "SELECT RU.username AS receiver_username, SU.username AS sender_username, " +
                "sender_id, receiver_id, sent, state, FR.id as id " +
                "FROM sn.friend_requests AS FR " +
                "JOIN sn.users AS RU on RU.id = FR.receiver_id " +
                "JOIN sn.users AS SU on SU.id = FR.sender_id " +
                "WHERE RU.username = ?";
        try (var s = connection.prepareStatement(cmd)) {
            s.setString(1, username);
            ArrayList<FriendRequestDTO> result = new ArrayList<>();
            try (var rs = s.executeQuery()) {
                while(rs.next())
                    result.add(extractFriendRequestDTO(rs));
                return result;
            }
        }
    }

    public Collection<FriendRequestDTO> requestsReceivedBy(Long accountId) throws SQLException {
        String cmd =
                "SELECT RU.username AS receiver_username, SU.username AS sender_username, " +
                "sender_id, receiver_id, sent, state, FR.id as id " +
                "FROM sn.friend_requests AS FR " +
                "JOIN sn.users AS RU on RU.id = FR.receiver_id " +
                "JOIN sn.users AS SU on SU.id = FR.sender_id " +
                "WHERE receiver_id = ?";
        try (var s = connection.prepareStatement(cmd)) {
            s.setLong(1, accountId);
            ArrayList<FriendRequestDTO> result = new ArrayList<>();
            try (var rs = s.executeQuery()) {
                while(rs.next())
                    result.add(extractFriendRequestDTO(rs));
                return result;
            }
        }
    }

    public void addRequest(ArrowIdPair idPair) throws SQLException {
        String cmd = "INSERT INTO sn.friend_requests " +
                "(sender_id, receiver_id, sent, state) VALUES " +
                "(?, ?, CURRENT_TIMESTAMP, 1)";
        try (var s = connection.prepareStatement(cmd)) {
            s.setLong(1, idPair.getSenderId());
            s.setLong(2, idPair.getReceiverId());
            s.executeUpdate();
        }
    }

    public void setRequestState(Long requestId, FriendRequest.State state) throws SQLException {
        String cmd = "UPDATE sn.friend_requests " +
                "SET state = ? " +
                "WHERE id = ? AND state = 1";
        try (var s = connection.prepareStatement(cmd)) {
            s.setInt(1, FriendRequest.getStateNumber(state));
            s.setLong(2, requestId);
            s.executeUpdate();
        }
    }

    public Collection<User> getPotentialFriends(String usernameFragment, Long currentUserId) {
        String cmd = "SELECT id, username FROM sn.users " +
                "WHERE id NOT IN " +
                "(SELECT u.id FROM sn.users u " +
                "JOIN sn.friend_requests frsent ON " +
                "u.id = frsent.sender_id AND frsent.receiver_id = ? AND frsent.state = 1 " +
                "UNION " +
                "SELECT u.id FROM sn.users u " +
                "JOIN sn.friend_requests frreceived ON " +
                "u.id = frreceived.receiver_id  AND frreceived.sender_id = ? AND frreceived.state = 1 " +
                "UNION " +
                "SELECT u.id FROM sn.users u " +
                "JOIN sn.friends f ON f.user_id = u.id " +
                "JOIN sn.friends f2 ON f2.friendship_id = f.friendship_id " +
                "WHERE f2.user_id = ?) " +
                "AND username LIKE ?";

        try (var s = connection.prepareStatement(cmd)) {
            s.setLong(1, currentUserId);
            s.setLong(2, currentUserId);
            s.setLong(3, currentUserId);
            s.setString(4, String.format("%%%s%%", usernameFragment));
            ArrayList<User> result = new ArrayList<>();
            try (var  rs = s.executeQuery()) {
                while(rs.next())
                    result.add(extractUser(rs));
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }

    public static FriendRequest extractFriendRequest(ResultSet rs) throws SQLException {
        var r =  new FriendRequest(
                rs.getLong("sender_id"),
                rs.getLong("receiver_id"),
                rs.getTimestamp("sent").toLocalDateTime(),
                rs.getInt("state")
        );
        r.setIdentifier(rs.getLong("id"));
        return r;
    }

    public static FriendRequestDTO extractFriendRequestDTO(ResultSet rs) throws SQLException {
        return new FriendRequestDTO(
                extractFriendRequest(rs),
                rs.getString("sender_username"),
                rs.getString("receiver_username")
        );
    }
}

package com.escript.data;

import com.escript.domain.Account;
import com.escript.domain.Message;
import com.escript.exceptions.DuplicateElementException;

import java.io.IOError;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class MessageDbRepo extends DbRepository {
    public MessageDbRepo() {
        super();
    }

    public long add(Message message) {
        String cmd = "INSERT INTO sn.messages (sender_id, receiver_id, date_sent, text) VALUES " +
                "(?, ?, CURRENT_TIMESTAMP, ?)";
        try (var s = connection.prepareStatement(cmd, Statement.RETURN_GENERATED_KEYS))
        {
            s.setLong(1, message.getIdPair().getSenderId());
            s.setLong(2, message.getIdPair().getReceiverId());
            s.setString(3, message.getText());
            s.executeUpdate();
            try (ResultSet keySet = s.getGeneratedKeys()) {
                keySet.next();
                return keySet.getLong(1);
            }
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    public Collection<Message> getConversation(IdPair idPair) {
        String cmd = "SELECT text, sender_id, receiver_id, date_sent, id FROM sn.messages " +
                "WHERE (sender_id = ? AND receiver_id = ?) OR " +
                "(sender_id = ? AND receiver_id = ?)" +
                "ORDER BY date_sent, id";

        try (var s = this.connection.prepareStatement(cmd)) {
            ArrayList<Message> result = new ArrayList<>();
            s.setLong(1, idPair.getFirst());
            s.setLong(2, idPair.getSecond());
            s.setLong(3, idPair.getSecond());
            s.setLong(4, idPair.getFirst());
            try (var rs = s.executeQuery()) {
                while (rs.next()) {
                    var m = new Message(
                            rs.getString("text"),
                            new ArrowIdPair(
                                    rs.getLong("sender_id"),
                                    rs.getLong("receiver_id")),
                            rs.getTimestamp("date_sent").toLocalDateTime()
                    );
                    m.setIdentifier(rs.getLong("id"));
                    result.add(m);
                }
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOError(e);
        }
    }
}

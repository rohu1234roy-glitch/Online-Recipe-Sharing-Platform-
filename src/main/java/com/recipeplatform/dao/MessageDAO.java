package com.recipeplatform.dao;

import com.recipeplatform.database.DatabaseConnection;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Data Access Object for messaging operations
public class MessageDAO {

    public boolean sendMessage(Message msg) throws DatabaseException {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, msg.getSenderId());
            ps.setInt(2, msg.getReceiverId());
            ps.setString(3, msg.getMessageText());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        msg.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to send message: " + e.getMessage(), e);
        }
    }

    public List<Message> getMessagesForUser(int userId) throws DatabaseException {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.*, u1.name as sender_name, u2.name as receiver_name " +
                     "FROM messages m " +
                     "JOIN users u1 ON m.sender_id = u1.id " +
                     "JOIN users u2 ON m.receiver_id = u2.id " +
                     "WHERE m.sender_id = ? OR m.receiver_id = ? " +
                     "ORDER BY m.sent_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Message(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            rs.getString("sender_name"),
                            rs.getInt("receiver_id"),
                            rs.getString("receiver_name"),
                            rs.getString("message_text"),
                            rs.getString("sent_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch messages: " + e.getMessage(), e);
        }
        return list;
    }
}

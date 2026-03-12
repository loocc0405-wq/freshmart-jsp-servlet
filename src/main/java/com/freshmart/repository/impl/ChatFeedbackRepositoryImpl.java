package com.freshmart.repository.impl;

import com.freshmart.entity.ChatFeedback;
import com.freshmart.repository.ChatFeedbackRepository;
import com.freshmart.util.JPAUtil;

import java.sql.*;

public class ChatFeedbackRepositoryImpl implements ChatFeedbackRepository {

    @Override
    public Long insert(ChatFeedback chatFeedback) {
        String sql = "INSERT INTO chat_feedback (chat_message_id, rating, comment, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = JPAUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setLong(1, chatFeedback.getChatMessageId());
            ps.setInt(2, chatFeedback.getRating());
            ps.setString(3, chatFeedback.getComment());
            
            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(4, now);
            
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    chatFeedback.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

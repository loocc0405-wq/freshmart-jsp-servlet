package com.freshmart.repository.impl;

import com.freshmart.entity.ChatMessage;
import com.freshmart.enums.ChatIntent;
import com.freshmart.enums.ChatRole;
import com.freshmart.enums.ChatSourceType;
import com.freshmart.repository.ChatMessageRepository;
import com.freshmart.util.JPAUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    @Override
    public Long insert(ChatMessage chatMessage) {
        String sql = "INSERT INTO chat_messages (chat_session_id, role, message_content, intent, source_type, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = JPAUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, chatMessage.getChatSessionId());
            ps.setString(2, chatMessage.getRole().name());
            ps.setString(3, chatMessage.getMessageContent());
            ps.setString(4, chatMessage.getIntent() != null ? chatMessage.getIntent().name() : null);
            ps.setString(5, chatMessage.getSourceType() != null ? chatMessage.getSourceType().name() : null);

            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(6, now);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    chatMessage.setId(id);
                    chatMessage.setCreatedAt(now);
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<ChatMessage> findBySessionId(Long sessionId) {
        List<ChatMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages WHERE chat_session_id = ? ORDER BY created_at ASC";

        try (Connection conn = JPAUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private ChatMessage mapRow(ResultSet rs) throws SQLException {
        ChatMessage cm = new ChatMessage();
        cm.setId(rs.getLong("id"));
        cm.setChatSessionId(rs.getLong("chat_session_id"));
        cm.setRole(ChatRole.valueOf(rs.getString("role")));
        cm.setMessageContent(rs.getString("message_content"));

        String intentStr = rs.getString("intent");
        if (intentStr != null) {
            cm.setIntent(ChatIntent.valueOf(intentStr));
        }

        String sourceStr = rs.getString("source_type");
        if (sourceStr != null) {
            cm.setSourceType(ChatSourceType.valueOf(sourceStr));
        }

        cm.setCreatedAt(rs.getTimestamp("created_at"));
        return cm;
    }
}
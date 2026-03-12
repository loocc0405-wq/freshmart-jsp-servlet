package com.freshmart.repository.impl;

import com.freshmart.entity.ChatSession;
import com.freshmart.repository.ChatSessionRepository;
import com.freshmart.util.JPAUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    @Override
    public Long insert(ChatSession chatSession) {
        String sql = "INSERT INTO chat_sessions (user_id, session_token, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = JPAUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (chatSession.getUserId() != null) {
                ps.setLong(1, chatSession.getUserId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }

            ps.setString(2, chatSession.getSessionToken());
            ps.setString(3, chatSession.getStatus());

            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    chatSession.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ChatSession findById(Long id) {
        String sql = "SELECT * FROM chat_sessions WHERE id = ?";

        try (Connection conn = JPAUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ChatSession findByToken(String token) {
        String sql = "SELECT * FROM chat_sessions WHERE session_token = ?";

        try (Connection conn = JPAUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<ChatSession> findByUserId(Long userId) {
        List<ChatSession> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_sessions WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = JPAUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

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

    @Override
    public void updateStatus(Long id, String status) {
        String sql = "UPDATE chat_sessions SET status = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = JPAUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setLong(3, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ChatSession mapRow(ResultSet rs) throws SQLException {
        ChatSession cs = new ChatSession();
        cs.setId(rs.getLong("id"));

        long userId = rs.getLong("user_id");
        if (!rs.wasNull()) {
            cs.setUserId(userId);
        }

        cs.setSessionToken(rs.getString("session_token"));
        cs.setStatus(rs.getString("status"));
        cs.setCreatedAt(rs.getTimestamp("created_at"));
        cs.setUpdatedAt(rs.getTimestamp("updated_at"));

        return cs;
    }
}
package com.freshmart.repository;

import com.freshmart.entity.ChatSession;
import java.util.List;

public interface ChatSessionRepository {
    Long insert(ChatSession chatSession);
    ChatSession findById(Long id);
    ChatSession findByToken(String token);
    List<ChatSession> findByUserId(Long userId);
    void updateStatus(Long id, String status);
}

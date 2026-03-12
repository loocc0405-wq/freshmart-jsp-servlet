package com.freshmart.repository;

import com.freshmart.entity.ChatMessage;
import java.util.List;

public interface ChatMessageRepository {
    Long insert(ChatMessage chatMessage);
    List<ChatMessage> findBySessionId(Long sessionId);
}

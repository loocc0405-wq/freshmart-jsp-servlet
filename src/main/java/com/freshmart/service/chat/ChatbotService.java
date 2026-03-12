package com.freshmart.service.chat;

import com.freshmart.entity.ChatMessage;
import com.freshmart.entity.ChatSession;
import com.freshmart.enums.ChatIntent;
import com.freshmart.enums.ChatRole;
import com.freshmart.enums.ChatSourceType;
import com.freshmart.repository.ChatMessageRepository;
import com.freshmart.repository.ChatSessionRepository;
import com.freshmart.repository.impl.ChatMessageRepositoryImpl;
import com.freshmart.repository.impl.ChatSessionRepositoryImpl;
import com.freshmart.service.ai.GeminiService;
import com.freshmart.service.ai.PromptBuilderService;

import java.util.UUID;

public class ChatbotService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatIntentService chatIntentService;
    private final PromptBuilderService promptBuilderService;
    private final GeminiService geminiService;

    public ChatbotService() {
        this.chatSessionRepository = new ChatSessionRepositoryImpl();
        this.chatMessageRepository = new ChatMessageRepositoryImpl();
        this.chatIntentService = new ChatIntentService();
        this.promptBuilderService = new PromptBuilderService();
        this.geminiService = new GeminiService();
    }

    public String processUserMessage(String sessionToken, Long userId, String userMessage) {
        
        // 1. Resolve Session
        ChatSession session = null;
        if (sessionToken != null && !sessionToken.isEmpty()) {
            session = chatSessionRepository.findByToken(sessionToken);
        }
        
        if (session == null) { // Create new session
            session = new ChatSession();
            session.setUserId(userId);
            session.setSessionToken(sessionToken != null && !sessionToken.isEmpty() ? sessionToken : UUID.randomUUID().toString());
            session.setStatus("active");
            Long id = chatSessionRepository.insert(session);
            session.setId(id);
        }

        // 2. Identify Intent
        ChatIntent intent = chatIntentService.determineIntent(userMessage);

        // 3. Save User Message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setChatSessionId(session.getId());
        userMsg.setRole(ChatRole.USER);
        userMsg.setMessageContent(userMessage);
        userMsg.setIntent(intent);
        chatMessageRepository.insert(userMsg);

        // 4. Build Prompt with Context Data based on Intent
        String fullPrompt = promptBuilderService.buildPrompt(intent, userMessage, userId);

        // 5. Query Gemini
        String aiResponse = geminiService.generateResponse(fullPrompt, userMessage);

        // 6. Save AI Response
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setChatSessionId(session.getId());
        aiMsg.setRole(ChatRole.ASSISTANT);
        aiMsg.setMessageContent(aiResponse);
        aiMsg.setIntent(intent);
        aiMsg.setSourceType(ChatSourceType.AI);
        chatMessageRepository.insert(aiMsg);

        return aiResponse;
    }
}

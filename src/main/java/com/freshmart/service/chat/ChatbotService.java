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
        String normalizedMessage = normalizeMessage(userMessage);
        if (normalizedMessage == null) {
            return "Bạn vui lòng nhập nội dung câu hỏi.";
        }

        String normalizedSessionToken = normalizeSessionToken(sessionToken);

        ChatSession session = resolveOrCreateSession(normalizedSessionToken, userId);
        ChatIntent intent = determineIntentSafely(normalizedMessage);

        saveUserMessageSafely(session, intent, normalizedMessage);

        String fullPrompt = buildPromptSafely(intent, normalizedMessage, userId);
        String aiResponse = generateReplySafely(intent, fullPrompt, normalizedMessage, userId);

        saveAssistantMessageSafely(session, intent, aiResponse);

        return aiResponse;
    }

    private ChatSession resolveOrCreateSession(String sessionToken, Long userId) {
        ChatSession session = null;

        try {
            session = chatSessionRepository.findByToken(sessionToken);
        } catch (Exception ignored) {
        }

        if (session != null) {
            return session;
        }

        ChatSession newSession = new ChatSession();
        newSession.setUserId(userId);
        newSession.setSessionToken(sessionToken);
        newSession.setStatus("active");

        try {
            Long id = chatSessionRepository.insert(newSession);
            newSession.setId(id);
        } catch (Exception ignored) {
        }

        return newSession;
    }

    private ChatIntent determineIntentSafely(String userMessage) {
        try {
            ChatIntent intent = chatIntentService.determineIntent(userMessage);
            return intent != null ? intent : ChatIntent.GENERAL_FAQ;
        } catch (Exception e) {
            return ChatIntent.GENERAL_FAQ;
        }
    }

    private String buildPromptSafely(ChatIntent intent, String userMessage, Long userId) {
        try {
            String prompt = promptBuilderService.buildPrompt(intent, userMessage, userId);
            if (prompt == null || prompt.isBlank()) {
                return userMessage;
            }
            return prompt;
        } catch (Exception e) {
            return userMessage;
        }
    }

    private String generateReplySafely(ChatIntent intent, String fullPrompt, String userMessage, Long userId) {
        try {
            String aiResponse = geminiService.generateResponse(fullPrompt, userMessage);
            if (aiResponse != null && !aiResponse.isBlank()) {
                return aiResponse.trim();
            }
        } catch (Exception ignored) {
        }

        return buildFallbackReply(intent, userId, userMessage);
    }

    private void saveUserMessageSafely(ChatSession session, ChatIntent intent, String userMessage) {
        if (session == null || session.getId() == null) {
            return;
        }

        try {
            ChatMessage userMsg = new ChatMessage();
            userMsg.setChatSessionId(session.getId());
            userMsg.setRole(ChatRole.USER);
            userMsg.setMessageContent(userMessage);
            userMsg.setIntent(intent);
            chatMessageRepository.insert(userMsg);
        } catch (Exception ignored) {
        }
    }

    private void saveAssistantMessageSafely(ChatSession session, ChatIntent intent, String aiResponse) {
        if (session == null || session.getId() == null || aiResponse == null || aiResponse.isBlank()) {
            return;
        }

        try {
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setChatSessionId(session.getId());
            aiMsg.setRole(ChatRole.ASSISTANT);
            aiMsg.setMessageContent(aiResponse);
            aiMsg.setIntent(intent);
            aiMsg.setSourceType(ChatSourceType.AI);
            chatMessageRepository.insert(aiMsg);
        } catch (Exception ignored) {
        }
    }

    private String buildFallbackReply(ChatIntent intent, Long userId, String userMessage) {
        if (intent == null) {
            intent = ChatIntent.GENERAL_FAQ;
        }

        switch (intent) {
            case PRODUCT_QUERY:
                return "Mình đang hỗ trợ tra cứu sản phẩm của FreshMart. Bạn hãy nói rõ tên sản phẩm bạn muốn tìm, ví dụ: táo, sữa, rau cải.";

            case ORDER_QUERY:
                if (userId == null) {
                    return "Bạn vui lòng đăng nhập để mình hỗ trợ kiểm tra đơn hàng.";
                }
                return "Mình đang hỗ trợ tra cứu đơn hàng của bạn. Bạn có thể gửi mã đơn hoặc hỏi tình trạng đơn hàng gần đây.";

            case POLICY_QUERY:
                return "FreshMart mở cửa từ 07:00 đến 21:00 mỗi ngày. Miễn phí giao hàng cho đơn từ 500,000 VND và hỗ trợ đổi trả trong 24h với thực phẩm tươi sống nếu lỗi từ cửa hàng.";

            case PROMOTION_QUERY:
                return "Bạn có thể xem khuyến mãi và voucher hiện có tại trang chủ hoặc khu vực ưu đãi của FreshMart.";

            case ACCOUNT_SUPPORT:
                return "Bạn có thể đăng nhập, cập nhật hồ sơ, xem lịch sử đơn hàng và theo dõi thông tin tài khoản trong hệ thống FreshMart.";

            case GENERAL_FAQ:
                return "Xin chào! Mình là chatbot hỗ trợ FreshMart. Bạn có thể hỏi về sản phẩm, đơn hàng, chính sách giao hàng hoặc tài khoản.";

            case OUT_OF_SCOPE:
            default:
                return "Mình có thể hỗ trợ về sản phẩm, đơn hàng, tài khoản, giao hàng và chính sách của FreshMart.";
        }
    }

    private String normalizeMessage(String userMessage) {
        if (userMessage == null) {
            return null;
        }

        String trimmed = userMessage.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeSessionToken(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return sessionToken.trim();
    }
}
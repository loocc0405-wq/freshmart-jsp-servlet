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
import com.freshmart.service.ai.AiForecastEngineService;
import com.freshmart.service.ai.GeminiService;
import com.freshmart.service.ai.PromptBuilderService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatbotService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatIntentService chatIntentService;
    private final PromptBuilderService promptBuilderService;
    private final GeminiService geminiService;
    private final AiForecastEngineService aiForecastEngineService;

    private static final int MAX_HISTORY_TURNS = 6;

    public ChatbotService() {
        this.chatSessionRepository = new ChatSessionRepositoryImpl();
        this.chatMessageRepository = new ChatMessageRepositoryImpl();
        this.chatIntentService = new ChatIntentService();
        this.promptBuilderService = new PromptBuilderService();
        this.geminiService = new GeminiService();
        this.aiForecastEngineService = new AiForecastEngineService();
    }

    public ChatResponse processUserMessage(String sessionToken, Long userId, String userMessage) {
        String normalizedMessage = normalizeMessage(userMessage);
        if (normalizedMessage == null) {
            return new ChatResponse("Bạn vui lòng nhập nội dung câu hỏi nhé! 😊", ChatIntent.GENERAL_FAQ);
        }

        String normalizedSessionToken = normalizeSessionToken(sessionToken);

        ChatSession session = resolveOrCreateSession(normalizedSessionToken, userId);
        ChatIntent intent = determineIntentSafely(normalizedMessage);

        saveUserMessageSafely(session, intent, normalizedMessage);

        String aiResponse;
        String localAnswer = tryLocalAnswer(normalizedMessage, userId);
        if (localAnswer != null) {
            System.out.println("[ChatbotService] Answered locally from DB data");
            aiResponse = localAnswer;
        } else {
            List<String[]> history = loadConversationHistory(session);
            String fullPrompt = buildPromptSafely(intent, normalizedMessage, userId);
            aiResponse = generateReplySafely(intent, fullPrompt, normalizedMessage, userId, history);
        }

        saveAssistantMessageSafely(session, intent, aiResponse);
        List<String> suggestedReplies = buildSuggestedReplies(intent);

        return new ChatResponse(aiResponse, intent, suggestedReplies);
    }

    private String tryLocalAnswer(String userMessage, Long userId) {
        try {
            ChatContextService contextService = new ChatContextService();
            return contextService.tryAnswerLocally(userMessage, userId);
        } catch (Exception e) {
            System.err.println("[ChatbotService] Local answer failed: " + e.getMessage());
            return null;
        }
    }

    private List<String[]> loadConversationHistory(ChatSession session) {
        if (session == null || session.getId() == null) {
            return List.of();
        }

        try {
            List<ChatMessage> messages = chatMessageRepository.findBySessionId(session.getId());
            if (messages == null || messages.isEmpty()) {
                return List.of();
            }

            List<String[]> history = new ArrayList<>();
            int startIdx = Math.max(0, messages.size() - MAX_HISTORY_TURNS);

            for (int i = startIdx; i < messages.size(); i++) {
                ChatMessage msg = messages.get(i);
                String role = (msg.getRole() == ChatRole.USER) ? "user" : "model";
                history.add(new String[]{role, msg.getMessageContent()});
            }

            return history;
        } catch (Exception e) {
            System.err.println("[ChatbotService] Error loading history: " + e.getMessage());
            return List.of();
        }
    }

    private List<String> buildSuggestedReplies(ChatIntent intent) {
        if (intent == null) intent = ChatIntent.GENERAL_FAQ;

        List<String> replies = new ArrayList<>();

        switch (intent) {
            case PRODUCT_QUERY:
                replies.add("Sản phẩm nào bán chạy?");
                replies.add("Có rau củ gì?");
                replies.add("Xem đơn hàng");
                break;

            case ORDER_QUERY:
                replies.add("Đơn hàng gần nhất");
                replies.add("Chính sách đổi trả");
                replies.add("Xem sản phẩm");
                break;

            case POLICY_QUERY:
                replies.add("Phí giao hàng?");
                replies.add("Đổi trả thế nào?");
                replies.add("Giờ mở cửa?");
                break;

            case PROMOTION_QUERY:
                replies.add("Có voucher nào không?");
                replies.add("Xem sản phẩm");
                replies.add("Chính sách giao hàng");
                break;

            case ACCOUNT_SUPPORT:
                replies.add("Quên mật khẩu");
                replies.add("Cập nhật hồ sơ");
                replies.add("Xem đơn hàng");
                break;

            case REVENUE_FORECAST:
                replies.add("Dự báo doanh thu quý tới");
                replies.add("Kế hoạch nhập hàng");
                replies.add("Cảnh báo biên lợi nhuận");
                replies.add("Phân tích mùa vụ");
                break;

            case GENERAL_FAQ:
                replies.add("Bạn bán gì?");
                replies.add("Tra đơn hàng");
                replies.add("Chính sách giao hàng");
                break;

            case OUT_OF_SCOPE:
            default:
                replies.add("Xem sản phẩm");
                replies.add("Tra đơn hàng");
                replies.add("Chính sách FreshMart");
                break;
        }

        return replies;
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
        } catch (Exception e) {
            System.err.println("[ChatbotService] Error creating session: " + e.getMessage());
        }

        return newSession;
    }

    private ChatIntent determineIntentSafely(String userMessage) {
        try {
            ChatIntent intent = chatIntentService.determineIntent(userMessage);
            return intent != null ? intent : ChatIntent.GENERAL_FAQ;
        } catch (Exception e) {
            System.err.println("[ChatbotService] Error determining intent: " + e.getMessage());
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
            System.err.println("[ChatbotService] Error building prompt: " + e.getMessage());
            return userMessage;
        }
    }

    private String generateReplySafely(ChatIntent intent, String fullPrompt, String userMessage, Long userId, List<String[]> history) {
        if (intent == ChatIntent.REVENUE_FORECAST) {
            try {
                String report = aiForecastEngineService.generateChatForecast(userMessage, null);
                if (report != null && !report.isBlank()) {
                    return report.trim();
                }
            } catch (Exception e) {
                System.err.println("[ChatbotService] Hybrid forecast reply failed: " + e.getMessage());
            }
        }

        try {
            String aiResponse = geminiService.generateResponse(fullPrompt, userMessage, history);
            if (aiResponse != null && !aiResponse.isBlank()) {
                return aiResponse.trim();
            }
        } catch (Exception e) {
            System.err.println("[ChatbotService] Error generating reply: " + e.getMessage());
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
        } catch (Exception e) {
            System.err.println("[ChatbotService] Error saving user message: " + e.getMessage());
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
        } catch (Exception e) {
            System.err.println("[ChatbotService] Error saving AI message: " + e.getMessage());
        }
    }

    private String buildFallbackReply(ChatIntent intent, Long userId, String userMessage) {
        if (intent == null) {
            intent = ChatIntent.GENERAL_FAQ;
        }

        switch (intent) {
            case PRODUCT_QUERY:
                return "🛒 **FreshMart** chuyên cung cấp thực phẩm tươi sạch bao gồm:\n" +
                       "- 🥬 Rau củ tươi\n" +
                       "- 🍎 Trái cây theo mùa\n" +
                       "- 🥩 Thịt tươi sống\n" +
                       "- 🐟 Hải sản\n" +
                       "- 🥛 Sữa & đồ uống\n\n" +
                       "Bạn muốn xem giá sản phẩm nào cụ thể? 😊";

            case ORDER_QUERY:
                if (userId == null) {
                    return "Bạn vui lòng đăng nhập để mình hỗ trợ kiểm tra đơn hàng nhé! 🔐";
                }
                return "📦 Mình hỗ trợ tra cứu đơn hàng. Bạn có thể xem đơn hàng tại mục **Đơn hàng của tôi** trên website nhé!";

            case POLICY_QUERY:
                return "📋 **Chính sách FreshMart:**\n" +
                       "- 🕐 Mở cửa: 07:00-21:00 hàng ngày\n" +
                       "- 🚚 Miễn phí ship đơn từ 500k\n" +
                       "- 🔄 Đổi trả trong 24h (thực phẩm tươi sống)\n" +
                       "- 💳 COD, chuyển khoản, ví điện tử\n" +
                       "- 📞 Hotline: 1900-xxxx";

            case PROMOTION_QUERY:
                return "Bạn có thể xem khuyến mãi mới nhất tại trang chủ FreshMart hoặc mục Ưu đãi nhé! 🎉";

            case ACCOUNT_SUPPORT:
                return "Mình hỗ trợ đăng nhập, đăng ký, cập nhật hồ sơ và xem lịch sử đơn hàng. Bạn cần hỗ trợ gì? 👤";

            case REVENUE_FORECAST:
                return "📊 **AI Engine Dự báo Doanh thu**\n" +
                       "Mình có thể hỗ trợ phân tích:\n" +
                       "- 📈 Dự báo doanh thu (tháng/quý/năm)\n" +
                       "- 🛒 Kế hoạch nhập hàng\n" +
                       "- ⚠️ Cảnh báo biên lợi nhuận\n" +
                       "- 🌿 Nhận diện mùa vụ\n\n" +
                       "Bạn muốn xem phân tích nào? 😊";

            case GENERAL_FAQ:
                return "Xin chào! Mình là **FreshBot** 🤖 — trợ lý AI của FreshMart.\n" +
                       "Mình có thể hỗ trợ về:\n" +
                       "- 🛒 Sản phẩm & giá cả\n" +
                       "- 📦 Đơn hàng\n" +
                       "- 🚚 Chính sách giao hàng & đổi trả\n" +
                       "- 👤 Tài khoản\n\n" +
                       "Bạn muốn hỏi gì nhé? 😊";

            case OUT_OF_SCOPE:
            default:
                return "Mình chuyên hỗ trợ về FreshMart! 😊 Bạn có thể hỏi về:\n" +
                       "- 🛒 Sản phẩm & giá cả\n" +
                       "- 📦 Đơn hàng\n" +
                       "- 🚚 Giao hàng & đổi trả\n" +
                       "- 👤 Tài khoản";
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

    public static class ChatResponse {
        private final String reply;
        private final ChatIntent intent;
        private final List<String> suggestedReplies;

        public ChatResponse(String reply, ChatIntent intent) {
            this(reply, intent, List.of());
        }

        public ChatResponse(String reply, ChatIntent intent, List<String> suggestedReplies) {
            this.reply = reply;
            this.intent = intent;
            this.suggestedReplies = suggestedReplies != null ? suggestedReplies : List.of();
        }

        public String getReply() { return reply; }
        public ChatIntent getIntent() { return intent; }
        public List<String> getSuggestedReplies() { return suggestedReplies; }
    }
}

package com.freshmart.service.ai;

import com.freshmart.enums.ChatIntent;
import com.freshmart.service.chat.ChatContextService;
import com.freshmart.util.AiConstants;
import com.freshmart.util.PromptTemplates;

public class PromptBuilderService {

    private final ChatContextService contextService;

    public PromptBuilderService() {
        this.contextService = new ChatContextService();
    }

    public String buildPrompt(ChatIntent intent, String userMessage, Long userId) {
        String baseSystem = AiConstants.SYSTEM_INSTRUCTION + "\n" + PromptTemplates.SYSTEM_ROLE_PROMPT;
        String contextPrompt = "";

        switch (intent) {
            case PRODUCT_QUERY:
                String prodCtx = contextService.buildProductContext();
                contextPrompt = PromptTemplates.buildProductContextPrompt(userMessage, prodCtx);
                break;
            case ORDER_QUERY:
                String orderCtx = contextService.buildOrderContext(userId);
                contextPrompt = PromptTemplates.buildOrderContextPrompt(userMessage, orderCtx);
                break;
            case POLICY_QUERY:
            case GENERAL_FAQ:
                String faqCtx = contextService.buildFaqContext();
                contextPrompt = PromptTemplates.buildFaqContextPrompt(userMessage, faqCtx);
                break;
            case OUT_OF_SCOPE:
                 return baseSystem + "\n[HƯỚNG DẪN]: TỪ CHỐI câu hỏi này vì nó nằm ngoài phạm vi hỗ trợ của siêu thị FreshMart. Câu hỏi: " + userMessage;
            default:
                contextPrompt = "Câu hỏi: " + userMessage;
                break;
        }

        return baseSystem + "\n\n" + contextPrompt;
    }
}

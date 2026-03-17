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
        String safeMessage = safe(userMessage);
        ChatIntent safeIntent = intent != null ? intent : ChatIntent.GENERAL_FAQ;

        String baseSystem = AiConstants.SYSTEM_INSTRUCTION + "\n" + PromptTemplates.SYSTEM_ROLE_PROMPT;
        String contextPrompt;

        switch (safeIntent) {
            case PRODUCT_QUERY: {
                String productContext = contextService.buildProductContext();
                contextPrompt = PromptTemplates.buildProductContextPrompt(safeMessage, productContext);
                break;
            }

            case ORDER_QUERY: {
                String orderContext = contextService.buildOrderContext(userId);
                contextPrompt = PromptTemplates.buildOrderContextPrompt(safeMessage, orderContext);
                break;
            }

            case POLICY_QUERY: {
                String faqContext = contextService.buildFaqContext();
                contextPrompt = PromptTemplates.buildFaqContextPrompt(safeMessage, faqContext);
                break;
            }

            case GENERAL_FAQ: {
                String faqContext = contextService.buildFaqContext();
                String productContext = contextService.buildProductContext();
                contextPrompt = "Người dùng đang hỏi chung.\n\n" +
                        "[CONTEXT - CHÍNH SÁCH]:\n" + faqContext + "\n\n" +
                        "[CONTEXT - SẢN PHẨM]:\n" + productContext + "\n\n" +
                        "[HƯỚNG DẪN]:\n" +
                        "- Chào hỏi thân thiện, giới thiệu bản thân là FreshBot.\n" +
                        "- Gợi ý các chủ đề có thể hỗ trợ: sản phẩm, đơn hàng, chính sách, tài khoản.\n" +
                        "- Nếu khách hỏi 'bán gì', liệt kê danh mục sản phẩm từ dữ liệu.\n\n" +
                        "[CÂU HỎI CỦA KHÁCH]: " + safeMessage;
                break;
            }

            case PROMOTION_QUERY: {
                String promotionContext = contextService.buildPromotionContext();
                contextPrompt = PromptTemplates.buildPromotionContextPrompt(safeMessage, promotionContext);
                break;
            }

            case ACCOUNT_SUPPORT: {
                contextPrompt = PromptTemplates.buildAccountContextPrompt(safeMessage);
                break;
            }

            case REVENUE_FORECAST: {
                AiForecastDataService forecastDataService = new AiForecastDataService();
                String inferredPeriod = inferForecastPeriod(safeMessage);
                String forecastContext = forecastDataService.buildForecastContext(inferredPeriod, null);
                contextPrompt = PromptTemplates.buildForecastContextPrompt(safeMessage, forecastContext);
                baseSystem = AiConstants.SYSTEM_INSTRUCTION + "\n" + AiConstants.FORECAST_SYSTEM_INSTRUCTION;
                break;
            }

            case OUT_OF_SCOPE: {
                contextPrompt = PromptTemplates.buildOutOfScopePrompt(safeMessage);
                break;
            }

            default:
                contextPrompt =
                        "Hãy trả lời ngắn gọn, tự nhiên, đúng phạm vi hỗ trợ của FreshMart.\n" +
                        "Câu hỏi khách hàng: " + safeMessage;
                break;
        }

        return baseSystem + "\n\n" + contextPrompt;
    }

    private String inferForecastPeriod(String message) {
        String safe = safe(message).toLowerCase();
        if (safe.contains("quý") || safe.contains("quy") || safe.contains("quarter")) {
            return "quarter";
        }
        if (safe.contains("năm") || safe.contains("nam") || safe.contains("year")) {
            return "year";
        }
        return "month";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

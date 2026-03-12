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

            case POLICY_QUERY:
            case GENERAL_FAQ: {
                String faqContext = contextService.buildFaqContext();
                contextPrompt = PromptTemplates.buildFaqContextPrompt(safeMessage, faqContext);
                break;
            }

            case PROMOTION_QUERY: {
                String faqContext = contextService.buildFaqContext();
                contextPrompt =
                        "NGỮ CẢNH KHUYẾN MÃI FRESHMART:\n" +
                        faqContext + "\n\n" +
                        "YÊU CẦU TRẢ LỜI:\n" +
                        "- Nếu chưa có dữ liệu khuyến mãi realtime, hãy nói rõ là cần kiểm tra trong hệ thống hoặc tại trang ưu đãi.\n" +
                        "- Không tự bịa voucher hoặc mức giảm giá.\n" +
                        "- Nếu người dùng hỏi chung chung, hãy gợi ý họ kiểm tra mục khuyến mãi hoặc nêu sản phẩm cụ thể.\n\n" +
                        "Câu hỏi khách hàng: " + safeMessage;
                break;
            }

            case ACCOUNT_SUPPORT: {
                contextPrompt =
                        "NGỮ CẢNH HỖ TRỢ TÀI KHOẢN FRESHMART:\n" +
                        "- Hỗ trợ đăng nhập, quên mật khẩu, cập nhật hồ sơ, lịch sử đơn hàng và thông tin tài khoản.\n" +
                        "- Nếu câu hỏi liên quan đến dữ liệu cá nhân hoặc đơn hàng riêng, ưu tiên nhắc người dùng đăng nhập.\n" +
                        "- Không tự bịa thông tin tài khoản nếu không có dữ liệu xác thực.\n\n" +
                        "Câu hỏi khách hàng: " + safeMessage;
                break;
            }

            case OUT_OF_SCOPE:
                return baseSystem + "\n\n" +
                        "[HƯỚNG DẪN TRẢ LỜI]\n" +
                        "- Từ chối lịch sự vì câu hỏi nằm ngoài phạm vi hỗ trợ của FreshMart.\n" +
                        "- Gợi ý người dùng hỏi về sản phẩm, đơn hàng, giao hàng, chính sách hoặc tài khoản.\n\n" +
                        "Câu hỏi khách hàng: " + safeMessage;

            default:
                contextPrompt =
                        "Hãy trả lời ngắn gọn, tự nhiên, đúng phạm vi hỗ trợ của FreshMart.\n" +
                        "Câu hỏi khách hàng: " + safeMessage;
                break;
        }

        return baseSystem + "\n\n" + contextPrompt;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
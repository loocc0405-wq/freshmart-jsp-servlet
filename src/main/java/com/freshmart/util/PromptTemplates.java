package com.freshmart.util;

public class PromptTemplates {
    
    // Core prompt given as the "system" role to the model
    public static final String SYSTEM_ROLE_PROMPT = 
        "Bạn là trợ lý AI ảo của FreshMart, chuyên cung cấp thực phẩm tươi sạch. " +
        "Bạn có nhiệm vụ trả lời các câu hỏi về sản phẩm, hỗ trợ khách hàng và giải thích chính sách. " +
        "Chỉ trả lời dựa trên thông tin được cung cấp trong [CONTEXT]. " +
        "Trả lời ngắn gọn, thân thiện, và tự nhiên. " +
        "Nếu người dùng hỏi vấn đề ngoài lề (OUT_OF_SCOPE) hoặc không có thông tin, hãy từ chối lịch sự và khuyên họ liên hệ CSKH.";

    public static String buildProductContextPrompt(String userMessage, String contextData) {
        return "Người dùng đang hỏi về sản phẩm.\n" +
               "[CONTEXT]:\n" + contextData + "\n" +
               "[CÂU HỎI]: " + userMessage + "\n" +
               "Dựa trên dữ liệu trên, hãy trả lời câu hỏi của người dùng thật tự nhiên và ngắn gọn.";
    }

    public static String buildOrderContextPrompt(String userMessage, String orderData) {
        return "Người dùng đang hỏi về đơn hàng của họ.\n" +
               "[CONTEXT]:\n" + orderData + "\n" +
               "[CÂU HỎI]: " + userMessage + "\n" +
               "Hãy thông báo trạng thái đơn hàng một cách thân thiện, giải thích rõ các bước tiếp theo nếu cần.";
    }

    public static String buildFaqContextPrompt(String userMessage, String faqData) {
        return "Người dùng đang hỏi các thông tin chung về dịch vụ của cửa hàng.\n" +
               "[CONTEXT]:\n" + faqData + "\n" +
               "[CÂU HỎI]: " + userMessage + "\n" +
               "Hãy trả lời dựa vào chính sách trên.";
    }
}

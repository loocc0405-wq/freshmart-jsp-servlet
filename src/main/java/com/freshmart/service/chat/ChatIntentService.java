package com.freshmart.service.chat;

import com.freshmart.enums.ChatIntent;

public class ChatIntentService {

    public ChatIntent determineIntent(String userMessage) {
        String lowerMsg = userMessage.toLowerCase();

        // Simple Rule-based Keyword matching to start with.
        if (lowerMsg.contains("đơn hàng") || lowerMsg.contains("mã đơn") || lowerMsg.contains("tình trạng giao")) {
            return ChatIntent.ORDER_QUERY;
        } else if (lowerMsg.contains("giá") || lowerMsg.contains("còn hàng") || lowerMsg.contains("sản phẩm") 
                   || lowerMsg.contains("táo") || lowerMsg.contains("cá") || lowerMsg.contains("rau")) {
            return ChatIntent.PRODUCT_QUERY;
        } else if (lowerMsg.contains("đổi trả") || lowerMsg.contains("ship") || lowerMsg.contains("bảo hành") || lowerMsg.contains("mở cửa")) {
            return ChatIntent.POLICY_QUERY;
        } else if (lowerMsg.contains("khuyến mãi") || lowerMsg.contains("giảm giá") || lowerMsg.contains("voucher")) {
            return ChatIntent.PROMOTION_QUERY;
        } else if (lowerMsg.contains("tài khoản") || lowerMsg.contains("đăng nhập") || lowerMsg.contains("mật khẩu")) {
            return ChatIntent.ACCOUNT_SUPPORT;
        } else if (lowerMsg.contains("chào") || lowerMsg.contains("hello") || lowerMsg.contains("giúp")) {
            return ChatIntent.GENERAL_FAQ;
        }

        // Default or Out of Scope
        return ChatIntent.OUT_OF_SCOPE;
    }
}

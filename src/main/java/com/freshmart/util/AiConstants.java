package com.freshmart.util;

public class AiConstants {
    // API Key should ideally be loaded from environment variables
    public static final String GEMINI_API_KEY = "AIzaSyCaDShWYkrfUlx-MhUDkmmKDwJ4ddWyvRs";
    public static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    // System Prompts Constants
    public static final String SYSTEM_INSTRUCTION = "Bạn là trợ lý AI ảo của FreshMart, một hệ thống chuyên cung cấp thực phẩm tươi sạch. " +
            "Nhiệm vụ của bạn là trả lời các câu hỏi về sản phẩm, hỗ trợ khách hàng và giải thích chính sách mua bán. " +
            "Hãy trả lời một cách lịch sự, thân thiện, súc tích và chính xác dựa trên dữ liệu hệ thống cung cấp. " +
            "Nếu bạn không chắc chắn về thông tin, hãy nói rõ là chưa có thông tin chính thức.";
    
    public static final String FALLBACK_RESPONSE = "Xin lỗi, hiện tại tôi chưa thể xử lý yêu cầu này. Vui lòng liên hệ bộ phận hỗ trợ khách hàng của FreshMart để được hỗ trợ chi tiết hơn.";
}

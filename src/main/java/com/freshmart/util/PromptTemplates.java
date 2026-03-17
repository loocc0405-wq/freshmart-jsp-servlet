package com.freshmart.util;

public class PromptTemplates {

    public static final String SYSTEM_ROLE_PROMPT =
        "Bạn là FreshBot, trợ lý AI của FreshMart — hệ thống bán lẻ thực phẩm tươi sạch. " +
        "Hãy trả lời dựa trên [CONTEXT] được cung cấp. " +
        "Ưu tiên dữ liệu thực tế trong context, không tự bịa thông tin. " +
        "Trả lời bằng tiếng Việt, thân thiện và tự nhiên.";

    public static String buildProductContextPrompt(String userMessage, String contextData) {
        return "Người dùng đang hỏi về sản phẩm của FreshMart.\n\n" +
               "[CONTEXT - DỮ LIỆU SẢN PHẨM]:\n" + contextData + "\n\n" +
               "[HƯỚNG DẪN TRẢ LỜI]:\n" +
               "- Nếu có sản phẩm phù hợp trong context, liệt kê tên, giá, đơn vị.\n" +
               "- Nếu khách hỏi chung 'bán gì', liệt kê các danh mục sản phẩm chính.\n" +
               "- Nếu khách hỏi cụ thể mà không tìm thấy, nói rõ chưa có trong hệ thống.\n" +
               "- Dùng emoji phù hợp (🥬🍎🥩🐟🥛).\n\n" +
               "[CÂU HỎI CỦA KHÁCH]: " + userMessage;
    }

    public static String buildOrderContextPrompt(String userMessage, String orderData) {
        return "Người dùng đang hỏi về đơn hàng.\n\n" +
               "[CONTEXT - DỮ LIỆU ĐƠN HÀNG]:\n" + orderData + "\n\n" +
               "[HƯỚNG DẪN TRẢ LỜI]:\n" +
               "- Nếu có thông tin đơn hàng, mô tả trạng thái rõ ràng.\n" +
               "- Giải thích các bước tiếp theo nếu đơn đang xử lý.\n" +
               "- Nếu chưa đăng nhập, nhắc khách đăng nhập trước.\n\n" +
               "[CÂU HỎI CỦA KHÁCH]: " + userMessage;
    }

    public static String buildFaqContextPrompt(String userMessage, String faqData) {
        return "Người dùng đang hỏi thông tin chung / chính sách của FreshMart.\n\n" +
               "[CONTEXT - CHÍNH SÁCH & FAQ]:\n" + faqData + "\n\n" +
               "[HƯỚNG DẪN TRẢ LỜI]:\n" +
               "- Trả lời dựa trên chính sách ở trên.\n" +
               "- Nếu câu hỏi ngoài phạm vi FAQ, nói rõ cần kiểm tra thêm.\n\n" +
               "[CÂU HỎI CỦA KHÁCH]: " + userMessage;
    }

    public static String buildPromotionContextPrompt(String userMessage, String contextData) {
        return "Người dùng đang hỏi về khuyến mãi / ưu đãi của FreshMart.\n\n" +
               "[CONTEXT]:\n" + contextData + "\n\n" +
               "[HƯỚNG DẪN TRẢ LỜI]:\n" +
               "- Nếu có thông tin khuyến mãi, liệt kê rõ ràng.\n" +
               "- Nếu chưa có dữ liệu, hướng dẫn khách xem trang ưu đãi hoặc liên hệ CSKH.\n" +
               "- Không tự bịa voucher hay mức giảm giá.\n\n" +
               "[CÂU HỎI CỦA KHÁCH]: " + userMessage;
    }

    public static String buildAccountContextPrompt(String userMessage) {
        return "Người dùng đang cần hỗ trợ về tài khoản FreshMart.\n\n" +
               "[CONTEXT - HỖ TRỢ TÀI KHOẢN]:\n" +
               "- FreshMart hỗ trợ: đăng nhập, đăng ký, quên mật khẩu, cập nhật hồ sơ.\n" +
               "- Khách có thể xem lịch sử đơn hàng, thông tin tài khoản sau khi đăng nhập.\n" +
               "- Nếu quên mật khẩu, sử dụng chức năng 'Quên mật khẩu' trên trang đăng nhập.\n\n" +
               "[HƯỚNG DẪN TRẢ LỜI]:\n" +
               "- Hướng dẫn cụ thể theo câu hỏi.\n" +
               "- Nhắc đăng nhập nếu cần truy cập thông tin cá nhân.\n\n" +
               "[CÂU HỎI CỦA KHÁCH]: " + userMessage;
    }

    public static String buildOutOfScopePrompt(String userMessage) {
        return "[HƯỚNG DẪN TRẢ LỜI]:\n" +
               "- Câu hỏi này nằm NGOÀI phạm vi hỗ trợ của FreshMart.\n" +
               "- Từ chối lịch sự, nhẹ nhàng.\n" +
               "- Gợi ý khách hỏi về: sản phẩm, đơn hàng, giao hàng, chính sách, tài khoản.\n" +
               "- Giữ câu trả lời ngắn gọn (1-2 câu).\n\n" +
               "[CÂU HỎI CỦA KHÁCH]: " + userMessage;
    }

    public static String buildForecastContextPrompt(String userMessage, String forecastData) {
        return "Người dùng đang yêu cầu dự báo/phân tích doanh thu FreshMart.\n\n" +
               "[CONTEXT - DỮ LIỆU ĐA NGUỒN CHO DỰ BÁO]:\n" + forecastData + "\n\n" +
               "[HƯỚNG DẪN PHÂN TÍCH]:\n" +
               "- Đưa ra DỰ BÁO DOANH THU với con số cụ thể và khoảng tin cậy.\n" +
               "- Phân tích XU HƯỚNG từ dữ liệu lịch sử và run-rate kỳ hiện tại.\n" +
               "- Đề xuất KẾ HOẠCH NHẬP HÀNG dựa trên tồn kho, lead time và xu hướng bán.\n" +
               "- CẢNH BÁO nếu giá nhập tăng nhanh hơn giá bán.\n" +
               "- Nhận diện MÙA VỤ, sự kiện marketing và proxy thời tiết sắp tới.\n" +
               "- Nếu context đã có baseline report, phải giữ nguyên các con số baseline.\n" +
               "- Trả lời ngắn gọn, dễ hiểu, dùng markdown và emoji.\n\n" +
               "[CÂU HỎI CỦA KHÁCH]: " + userMessage;
    }
}

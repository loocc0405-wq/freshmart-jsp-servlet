package com.freshmart.service.chat;

public class ChatContextService {

    public ChatContextService() {
    }

    public String buildProductContext() {
        return String.join("\n",
                "NGỮ CẢNH SẢN PHẨM FRESHMART:",
                "- FreshMart kinh doanh thực phẩm tươi sống, rau củ, trái cây, cá, thịt, sữa và hàng tiêu dùng thiết yếu.",
                "- Khi khách hỏi về sản phẩm, hãy ưu tiên trả lời ngắn gọn, rõ ràng, thân thiện.",
                "- Nếu khách hỏi giá hoặc còn hàng mà chưa có dữ liệu realtime, hãy nói rõ rằng cần kiểm tra trực tiếp trong hệ thống.",
                "- Nếu khách chỉ hỏi chung chung, hãy gợi ý họ nêu tên sản phẩm cụ thể.",
                "- Ví dụ nhóm sản phẩm thường gặp: táo, cam, chuối, rau cải, cá hồi, sữa tươi.",
                "- Không tự bịa tồn kho, giá bán hoặc khuyến mãi nếu không có dữ liệu xác thực.",
                "",
                "MẪU TRẢ LỜI THAM KHẢO:",
                "- 'Bạn muốn tìm sản phẩm nào tại FreshMart? Mình có thể hỗ trợ theo tên sản phẩm cụ thể.'",
                "- 'Mình có thể hỗ trợ tra cứu thông tin sản phẩm, giá tham khảo và tình trạng còn hàng nếu hệ thống có dữ liệu.'");
    }

    public String buildOrderContext(Long userId) {
        if (userId == null) {
            return String.join("\n",
                    "NGỮ CẢNH ĐƠN HÀNG FRESHMART:",
                    "- Người dùng hiện chưa đăng nhập.",
                    "- Không cung cấp thông tin đơn hàng cá nhân khi chưa xác định được người dùng.",
                    "- Hãy yêu cầu khách đăng nhập để kiểm tra đơn hàng, lịch sử mua hàng hoặc trạng thái giao hàng.",
                    "",
                    "MẪU TRẢ LỜI THAM KHẢO:",
                    "- 'Bạn vui lòng đăng nhập để mình hỗ trợ kiểm tra đơn hàng của bạn.'",
                    "- 'Sau khi đăng nhập, bạn có thể hỏi tình trạng đơn hàng hoặc gửi mã đơn để mình hỗ trợ tốt hơn.'");
        }

        return String.join("\n",
                "NGỮ CẢNH ĐƠN HÀNG FRESHMART:",
                "- Người dùng đã đăng nhập với userId = " + userId + ".",
                "- Có thể hỗ trợ các câu hỏi về trạng thái đơn hàng, lịch sử mua hàng, mã đơn, giao hàng và tổng quan đơn gần đây.",
                "- Nếu khách chưa cung cấp mã đơn, có thể hướng dẫn họ gửi mã đơn để tra cứu chính xác hơn.",
                "- Không tự bịa trạng thái đơn hàng nếu chưa có dữ liệu xác thực từ hệ thống.",
                "",
                "TRẠNG THÁI ĐƠN HÀNG THƯỜNG GẶP:",
                "- PENDING: Đơn mới tạo, đang chờ xử lý.",
                "- PROCESSING: Đơn đang được chuẩn bị.",
                "- SHIPPING: Đơn đang được giao.",
                "- COMPLETED: Đơn đã hoàn tất.",
                "- CANCELED: Đơn đã bị hủy.",
                "",
                "MẪU TRẢ LỜI THAM KHẢO:",
                "- 'Bạn có thể gửi mã đơn để mình hỗ trợ kiểm tra nhanh hơn.'",
                "- 'Mình có thể hỗ trợ tra cứu trạng thái đơn hàng, lịch sử mua hàng và tiến độ giao hàng của bạn.'");
    }

    public String buildFaqContext() {
        return String.join("\n",
                "CHÍNH SÁCH / FAQ FRESHMART:",
                "- Giờ mở cửa: 07:00 - 21:00 hàng ngày.",
                "- Miễn phí giao hàng cho đơn từ 500,000 VND.",
                "- Hỗ trợ đổi trả trong vòng 24h đối với thực phẩm tươi sống nếu có lỗi từ cửa hàng.",
                "- Không hỗ trợ thanh toán COD cho đơn trên 2,000,000 VND.",
                "- Có thể hỗ trợ các câu hỏi về giao hàng, đổi trả, thanh toán, tài khoản và mua sắm cơ bản.",
                "",
                "HƯỚNG DẪN TRẢ LỜI:",
                "- Trả lời rõ ràng, ưu tiên tiếng Việt tự nhiên.",
                "- Nếu khách hỏi về phí ship, hãy nhắc ngưỡng miễn phí giao hàng.",
                "- Nếu khách hỏi về đổi trả, hãy nêu điều kiện 24h và lỗi từ cửa hàng.",
                "- Nếu khách hỏi ngoài phạm vi chính sách hiện có, hãy trả lời trung thực rằng cần kiểm tra thêm.",
                "",
                "MẪU TRẢ LỜI THAM KHẢO:",
                "- 'FreshMart mở cửa từ 07:00 đến 21:00 mỗi ngày.'",
                "- 'FreshMart miễn phí giao hàng cho đơn từ 500,000 VND.'",
                "- 'FreshMart hỗ trợ đổi trả trong vòng 24h đối với thực phẩm tươi sống nếu có lỗi từ cửa hàng.'");
    }
}
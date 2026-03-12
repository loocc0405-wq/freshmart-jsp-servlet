package com.freshmart.service.chat;

import com.freshmart.service.OrderService;
import com.freshmart.service.ProductService;

public class ChatContextService {

    private final ProductService productService;
    private final OrderService orderService;

    public ChatContextService() {
        this.productService = new ProductService();
        this.orderService = new OrderService();
    }

    public String buildProductContext() {
        return "Danh sách sản phẩm hiện tại đang được đồng bộ từ hệ thống FreshMart. " +
               "Hiện chưa cấu hình truy vấn chi tiết sản phẩm trong ChatContextService.";
    }

    public String buildOrderContext(Long userId) {
        if (userId == null) {
            return "Khách hàng chưa đăng nhập. Không thể tra cứu đơn hàng cá nhân.";
        }

        return "Thông tin đơn hàng của khách đang được lấy từ hệ thống FreshMart. " +
               "Hiện chưa cấu hình truy vấn chi tiết đơn hàng trong ChatContextService cho userId = " + userId + ".";
    }

    public String buildFaqContext() {
        return "Chính sách Cửa hàng FreshMart:\n" +
               "- Giờ mở cửa: 07:00 - 21:00 hàng ngày.\n" +
               "- Miễn phí giao hàng cho đơn từ 500,000 VND.\n" +
               "- Hỗ trợ đổi trả trong vòng 24h đối với thực phẩm tươi sống nếu có lỗi từ cửa hàng.\n" +
               "- Không hỗ trợ thanh toán COD cho đơn trên 2,000,000 VND.";
    }
}
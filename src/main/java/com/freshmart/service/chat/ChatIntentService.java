package com.freshmart.service.chat;

import com.freshmart.enums.ChatIntent;

import java.text.Normalizer;
import java.util.Locale;

public class ChatIntentService {

    public ChatIntent determineIntent(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return ChatIntent.GENERAL_FAQ;
        }

        String raw = userMessage.toLowerCase(Locale.ROOT).trim();
        String normalized = normalize(raw);

        if (containsAny(raw, normalized,
                "đơn hàng", "ma don", "mã đơn", "don hang", "order",
                "tình trạng giao", "tinh trang giao", "giao đến đâu", "giao den dau",
                "đơn của tôi", "don cua toi", "lịch sử mua", "lich su mua")) {
            return ChatIntent.ORDER_QUERY;
        }

        if (containsAny(raw, normalized,
                "giá", "gia", "bao nhiêu", "bao nhieu",
                "còn hàng", "con hang", "hết hàng", "het hang",
                "sản phẩm", "san pham", "mặt hàng", "mat hang",
                "trái cây", "trai cay", "rau", "cá", "ca", "thịt", "thit",
                "táo", "tao", "cam", "chuối", "chuoi", "sữa", "sua",
                "bán gì", "ban gi", "bán những gì", "ban nhung gi",
                "có gì", "co gi", "danh mục", "danh muc",
                "loại nào", "loai nao", "nhóm hàng", "nhom hang",
                "bán chạy", "ban chay", "best seller", "phổ biến", "pho bien",
                "có rau", "co rau", "có thịt", "co thit",
                "có cá", "co ca", "có trái", "co trai",
                "món gì", "mon gi", "hàng gì", "hang gi",
                "sữa", "sua", "đồ uống", "do uong", "nước", "nuoc",
                "tồn kho", "ton kho", "còn không", "con khong")) {
            return ChatIntent.PRODUCT_QUERY;
        }

        if (containsAny(raw, normalized,
                "đổi trả", "doi tra", "hoàn tiền", "hoan tien",
                "ship", "giao hàng", "giao hang", "phí ship", "phi ship",
                "free ship", "miễn phí giao", "mien phi giao",
                "mở cửa", "mo cua", "đóng cửa", "dong cua",
                "bảo hành", "bao hanh", "chính sách", "chinh sach",
                "policy", "thanh toán", "thanh toan", "cod",
                "giờ mở cửa", "gio mo cua", "mấy giờ", "may gio",
                "liên hệ", "lien he", "hotline", "contact",
                "số điện thoại", "so dien thoai", "email")) {
            return ChatIntent.POLICY_QUERY;
        }

        if (containsAny(raw, normalized,
                "khuyến mãi", "khuyen mai", "giảm giá", "giam gia",
                "voucher", "mã giảm giá", "ma giam gia",
                "ưu đãi", "uu dai", "combo")) {
            return ChatIntent.PROMOTION_QUERY;
        }

        if (containsAny(raw, normalized,
                "tài khoản", "tai khoan", "đăng nhập", "dang nhap",
                "đăng ký", "dang ky", "mật khẩu", "mat khau",
                "quên mật khẩu", "quen mat khau", "hồ sơ", "ho so",
                "profile", "thông tin cá nhân", "thong tin ca nhan")) {
            return ChatIntent.ACCOUNT_SUPPORT;
        }

        if (containsAny(raw, normalized,
                "dự báo", "du bao", "forecast", "prediction",
                "dự đoán doanh thu", "du doan doanh thu",
                "doanh thu tháng tới", "doanh thu thang toi",
                "doanh thu quý tới", "doanh thu quy toi",
                "kế hoạch nhập hàng", "ke hoach nhap hang",
                "procurement", "biên lợi nhuận", "bien loi nhuan",
                "phân tích doanh thu", "phan tich doanh thu",
                "xu hướng doanh thu", "xu huong doanh thu",
                "cảnh báo giá", "canh bao gia",
                "ai forecast", "ai engine", "ai dự báo")) {
            return ChatIntent.REVENUE_FORECAST;
        }

        if (containsAny(raw, normalized,
                "chào", "xin chào", "hello", "hi", "hey",
                "giúp", "giup", "hỗ trợ", "ho tro",
                "hướng dẫn", "huong dan", "support",
                "freshmart", "fresh mart",
                "cảm ơn", "cam on", "thanks", "tks", "cám ơn")) {
            return ChatIntent.GENERAL_FAQ;
        }

        return ChatIntent.OUT_OF_SCOPE;
    }

    private boolean containsAny(String raw, String normalized, String... keywords) {
        for (String keyword : keywords) {
            String kw = keyword.toLowerCase(Locale.ROOT).trim();
            String normalizedKw = normalize(kw);

            if (raw.contains(kw) || normalized.contains(normalizedKw)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String input) {
        String value = Normalizer.normalize(input, Normalizer.Form.NFD);
        value = value.replaceAll("\\p{M}", "");
        value = value.replace('đ', 'd').replace('Đ', 'D');
        return value.toLowerCase(Locale.ROOT);
    }
}
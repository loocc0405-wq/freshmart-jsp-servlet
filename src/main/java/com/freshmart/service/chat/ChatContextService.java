package com.freshmart.service.chat;

import com.freshmart.entity.Order;
import com.freshmart.entity.OrderItem;
import com.freshmart.entity.Product;
import com.freshmart.entity.RevenueDaily;
import com.freshmart.repository.OrderRepository;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.RevenueDailyRepository;
import com.freshmart.service.ai.AiForecastDataService;
import com.freshmart.util.JpaExecutor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Service that builds REAL context from the database for the AI chatbot.
 * Uses JpaExecutor for reliable EntityManager lifecycle management.
 */
public class ChatContextService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final RevenueDailyRepository revenueRepo;
    private final JpaExecutor executor;


    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    public ChatContextService() {
        this.productRepository = new ProductRepository();
        this.orderRepository = new OrderRepository();
        this.revenueRepo = new RevenueDailyRepository();
        this.executor = new JpaExecutor();
    }

    /**
     * Build product context with REAL data from the database.
     */
    public String buildProductContext() {
        try {
            return executor.execute(em -> {
                StringBuilder sb = new StringBuilder();
                sb.append("DANH MỤC SẢN PHẨM FRESHMART:\n");

                // Get all categories
                List<String> categories = productRepository.listCategories(em);
                if (!categories.isEmpty()) {
                    sb.append("Các nhóm hàng: ").append(String.join(", ", categories)).append("\n\n");
                }

                // Get all active products with prices
                List<Product> products = productRepository.findAll(em, false);
                if (!products.isEmpty()) {
                    sb.append("CHI TIẾT SẢN PHẨM ĐANG BÁN (").append(products.size()).append(" sản phẩm):\n");
                    for (Product p : products) {
                        sb.append("- ").append(p.getName());
                        if (p.getCategory() != null) {
                            sb.append(" [").append(p.getCategory()).append("]");
                        }
                        if (p.getSellPrice() != null && p.getSellPrice().compareTo(BigDecimal.ZERO) > 0) {
                            sb.append(" — Giá: ").append(formatVND(p.getSellPrice()));
                        }
                        if (p.getUnit() != null && !p.getUnit().isBlank()) {
                            sb.append("/").append(p.getUnit());
                        }
                        sb.append("\n");
                    }
                } else {
                    sb.append("- Hiện chưa có dữ liệu sản phẩm.\n");
                }

                sb.append("\nHƯỚNG DẪN: Trả lời dựa trên danh sách sản phẩm thực tế ở trên. Khi khách hỏi giá, trả lời chính xác từ dữ liệu.\n");
                return sb.toString();
            });
        } catch (Exception e) {
            System.err.println("[ChatContextService] Error loading products: " + e.getMessage());
            return "DANH MỤC SẢN PHẨM: Tạm thời chưa thể tải dữ liệu sản phẩm.\n";
        }
    }

    /**
     * Build order context with REAL data for logged-in users.
     */
    public String buildOrderContext(Long userId) {
        if (userId == null) {
            return "THÔNG TIN ĐƠN HÀNG:\n- Người dùng CHƯA đăng nhập.\n- Hãy yêu cầu khách đăng nhập để kiểm tra đơn hàng.\n";
        }

        try {
            return executor.execute(em -> {
                StringBuilder sb = new StringBuilder();
                sb.append("THÔNG TIN ĐƠN HÀNG CỦA KHÁCH (userId=").append(userId).append("):\n\n");

                List<Order> orders = orderRepository.findByCustomerId(em, userId);
                long totalOrders = orderRepository.countOrdersByCustomer(em, userId);

                sb.append("Tổng số đơn hàng: ").append(totalOrders).append("\n\n");

                if (orders.isEmpty()) {
                    sb.append("- Khách hàng chưa có đơn hàng nào.\n");
                } else {
                    int showCount = Math.min(orders.size(), 5);
                    sb.append("ĐƠN HÀNG GẦN ĐÂY (").append(showCount).append(" đơn mới nhất):\n");

                    for (int i = 0; i < showCount; i++) {
                        Order o = orders.get(i);
                        sb.append("📦 Đơn #").append(o.getId());
                        sb.append(" (Mã: ").append(o.getOrderCode()).append(")");
                        if (o.getStatus() != null) {
                            sb.append(" | Trạng thái: ").append(translateStatus(o.getStatus().name()));
                        }
                        if (o.getTotalAmount() != null) {
                            sb.append(" | Tổng: ").append(formatVND(o.getTotalAmount()));
                        }
                        if (o.getCreatedAt() != null) {
                            sb.append(" | Ngày: ").append(o.getCreatedAt().toLocalDate());
                        }
                        sb.append("\n");

                        List<OrderItem> items = o.getItems();
                        if (items != null && !items.isEmpty()) {
                            for (OrderItem item : items) {
                                sb.append("   - ");
                                if (item.getProduct() != null) {
                                    sb.append(item.getProduct().getName());
                                } else {
                                    sb.append("Sản phẩm");
                                }
                                sb.append(" x").append(item.getQuantity());
                                if (item.getUnitPrice() != null) {
                                    sb.append(" (").append(formatVND(item.getUnitPrice())).append(")");
                                }
                                sb.append("\n");
                            }
                        }
                    }

                    BigDecimal totalSpent = orderRepository.getTotalSpentByCustomer(em, userId);
                    if (totalSpent.compareTo(BigDecimal.ZERO) > 0) {
                        sb.append("\n💰 Tổng chi tiêu (đơn hoàn tất): ").append(formatVND(totalSpent)).append("\n");
                    }
                }

                sb.append("\nTRẠNG THÁI: PENDING=Chờ xử lý, PROCESSING=Đang chuẩn bị, SHIPPING=Đang giao, COMPLETED=Hoàn tất, CANCELED=Đã hủy\n");
                return sb.toString();
            });
        } catch (Exception e) {
            System.err.println("[ChatContextService] Error loading orders: " + e.getMessage());
            return "THÔNG TIN ĐƠN HÀNG: Tạm thời chưa thể tải dữ liệu đơn hàng.\n";
        }
    }

    /**
     * Build revenue/forecast summary from revenue_daily table.
     */
    public String buildRevenueContext() {
        try {
            return executor.execute(em -> {
                StringBuilder sb = new StringBuilder();
                sb.append("DỮ LIỆU DOANH THU FRESHMART:\n\n");

                LocalDate today = LocalDate.now();
                LocalDate last30 = today.minusDays(30);
                LocalDate last7 = today.minusDays(7);

                List<RevenueDaily> last30Days = revenueRepo.findBetween(em, last30, today);

                if (last30Days.isEmpty()) {
                    sb.append("- Chưa có dữ liệu doanh thu.\n");
                    return sb.toString();
                }

                // Total revenue last 30 days
                BigDecimal total30 = last30Days.stream()
                        .map(RevenueDaily::getTotalRevenue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Total revenue last 7 days
                BigDecimal total7 = last30Days.stream()
                        .filter(r -> !r.getRevenueDate().isBefore(last7))
                        .map(RevenueDaily::getTotalRevenue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Average daily
                BigDecimal avg = total30.divide(BigDecimal.valueOf(Math.max(1, last30Days.size())), 0, RoundingMode.HALF_UP);

                sb.append("📊 Doanh thu 30 ngày gần nhất: ").append(formatVND(total30)).append("\n");
                sb.append("📊 Doanh thu 7 ngày gần nhất: ").append(formatVND(total7)).append("\n");
                sb.append("📊 Doanh thu trung bình mỗi ngày: ").append(formatVND(avg)).append("\n");
                sb.append("📊 Số ngày có dữ liệu: ").append(last30Days.size()).append(" ngày\n");

                return sb.toString();
            });
        } catch (Exception e) {
            System.err.println("[ChatContextService] Error loading revenue: " + e.getMessage());
            return "DỮ LIỆU DOANH THU: Tạm thời chưa thể tải.\n";
        }
    }

    /**
     * Build FAQ / policy context.
     */
    public String buildFaqContext() {
        return String.join("\n",
                "CHÍNH SÁCH FRESHMART:",
                "",
                "🕐 GIỜ MỞ CỬA: 07:00 - 21:00 hàng ngày (kể cả cuối tuần và ngày lễ).",
                "🚚 GIAO HÀNG: Miễn phí giao hàng cho đơn từ 500,000 VND. Phí ship 25,000 VND cho đơn dưới 500k. Giao trong 1-2h nội thành.",
                "🔄 ĐỔI TRẢ: Đổi trả trong 24h với thực phẩm tươi sống nếu lỗi từ cửa hàng. Hàng phải còn nguyên bao bì.",
                "💳 THANH TOÁN: Hỗ trợ tiền mặt (COD), chuyển khoản, ví điện tử. Không COD cho đơn trên 2,000,000 VND.",
                "📞 LIÊN HỆ: Hotline 1900-xxxx (8:00-20:00).");
    }

    /**
     * Build promotion context.
     */
    public String buildPromotionContext() {
        return "KHUYẾN MÃI FRESHMART:\n" +
                "- Thường xuyên có chương trình khuyến mãi và voucher.\n" +
                "- Kiểm tra trang Ưu đãi trên website để biết chi tiết.\n" +
                "- Không có dữ liệu khuyến mãi realtime.\n";
    }

    /**
     * Try to answer a question directly from DB without needing AI.
     * Returns null if the question cannot be answered locally.
     */
    public String tryAnswerLocally(String userMessage, Long userId) {
        if (userMessage == null) return null;
        String msg = userMessage.toLowerCase().trim();

        // Greeting — check first
        if (matchesAny(msg, "xin chào", "chào", "hello", "hi", "hey")) {
            return "Xin chào bạn! 👋 Mình là **FreshBot** — trợ lý AI của FreshMart.\n" +
                    "Mình có thể hỗ trợ bạn về:\n" +
                    "- 🛒 Tra cứu sản phẩm & giá cả\n" +
                    "- 📦 Kiểm tra đơn hàng\n" +
                    "- 🚚 Chính sách giao hàng & đổi trả\n\n" +
                    "Bạn muốn hỏi gì nhé? 😊";
        }

        // Thanking
        if (matchesAny(msg, "cảm ơn", "cam on", "thank", "thanks", "tks", "ok cảm ơn", "cám ơn")) {
            return "Không có gì ạ! 😊 Nếu cần hỗ trợ thêm, bạn cứ hỏi mình nhé!";
        }

        // "sản phẩm bán chạy" / "best seller" — check BEFORE generic product match
        if (matchesAny(msg, "bán chạy", "ban chay", "best seller", "phổ biến", "pho bien",
                "top sản phẩm", "top san pham", "mua nhiều", "mua nhieu")) {
            return buildLocalBestSellerReply();
        }

        // "bán gì" / "có gì" / "danh mục" — list products
        if (matchesAny(msg, "bán gì", "ban gi", "bán những gì", "ban nhung gi", "có gì", "co gi",
                "danh mục", "danh muc", "loại nào", "loai nao", "nhóm hàng", "nhom hang",
                "món gì", "mon gi", "hàng gì", "hang gi")) {
            return buildLocalProductListReply();
        }

        // "giá" / "bao nhiêu" — product price query
        if (matchesAny(msg, "giá", "gia", "bao nhiêu", "bao nhieu")) {
            return buildLocalPriceReply(msg);
        }

        // "rau" / "cá" / "thịt" / specific category queries
        if (matchesAny(msg, "rau", "cá", "ca ", "thịt", "thit", "trái cây", "trai cay",
                "hải sản", "hai san", "có rau", "co rau", "có thịt", "co thit",
                "có cá", "co ca", "có trái", "co trai",
                "sữa", "sua", "đồ uống", "do uong", "nước", "nuoc")) {
            return buildLocalCategoryReply(msg);
        }

        // Generic "sản phẩm" — list products
        if (matchesAny(msg, "sản phẩm", "san pham", "mặt hàng", "mat hang")) {
            return buildLocalProductListReply();
        }

        // Policy queries - specific
        if (matchesAny(msg, "giao hàng", "giao hang", "ship", "phí ship", "phi ship", "free ship")) {
            return "🚚 **Chính sách giao hàng FreshMart:**\n" +
                    "- Miễn phí giao hàng cho đơn từ **500,000 VND**\n" +
                    "- Phí ship 25,000 VND cho đơn dưới 500k\n" +
                    "- Giao trong 1-2h nội thành, 2-4h ngoại thành";
        }

        if (matchesAny(msg, "đổi trả", "doi tra", "trả hàng", "tra hang", "hoàn tiền", "hoan tien")) {
            return "🔄 **Chính sách đổi trả FreshMart:**\n" +
                    "- Đổi trả trong vòng **24h** với thực phẩm tươi sống nếu lỗi từ cửa hàng\n" +
                    "- Sản phẩm phải còn nguyên bao bì\n" +
                    "- Liên hệ hotline 1900-xxxx để được hỗ trợ";
        }

        if (matchesAny(msg, "giờ mở cửa", "gio mo cua", "mở cửa", "mo cua", "đóng cửa", "dong cua",
                "mấy giờ", "may gio")) {
            return "🕐 FreshMart mở cửa từ **07:00 đến 21:00** hàng ngày, kể cả cuối tuần và ngày lễ!";
        }

        if (matchesAny(msg, "thanh toán", "thanh toan", "trả tiền", "tra tien", "cod", "chuyển khoản", "chuyen khoan")) {
            return "💳 **Phương thức thanh toán FreshMart:**\n" +
                    "- Tiền mặt (COD)\n" +
                    "- Chuyển khoản ngân hàng\n" +
                    "- Ví điện tử\n" +
                    "- Lưu ý: Không hỗ trợ COD cho đơn trên 2,000,000 VND";
        }

        // Contact info
        if (matchesAny(msg, "liên hệ", "lien he", "hotline", "số điện thoại", "so dien thoai",
                "gọi", "goi", "contact", "email")) {
            return "📞 **Liên hệ FreshMart:**\n" +
                    "- Hotline: **1900-xxxx** (8:00-20:00)\n" +
                    "- Email: support@freshmart.vn\n" +
                    "- Cửa hàng mở cửa: 07:00-21:00 hàng ngày";
        }

        // Stock queries
        if (matchesAny(msg, "còn hàng", "con hang", "tồn kho", "ton kho", "hết hàng", "het hang",
                "còn không", "con khong")) {
            return buildLocalProductListReply();
        }

        // "chính sách" general
        if (matchesAny(msg, "chính sách", "chinh sach", "policy", "freshmart")) {
            return "📋 **Chính sách FreshMart:**\n" +
                    "- 🕐 Mở cửa: 07:00-21:00 hàng ngày\n" +
                    "- 🚚 Miễn phí ship đơn từ 500k\n" +
                    "- 🔄 Đổi trả trong 24h (thực phẩm tươi sống)\n" +
                    "- 💳 COD, chuyển khoản, ví điện tử\n" +
                    "- 📞 Hotline: 1900-xxxx";
        }

        // Order query for logged-in users
        if (matchesAny(msg, "đơn hàng", "don hang", "đơn của tôi", "don cua toi", "tra đơn", "tra don",
                "order", "mã đơn", "ma don")) {
            return buildLocalOrderReply(userId);
        }

        // "dự báo" / "forecast" / AI forecast queries
        if (matchesAny(msg, "dự báo", "du bao", "forecast", "prediction",
                "dự đoán doanh thu", "du doan doanh thu",
                "kế hoạch nhập hàng", "ke hoach nhap hang",
                "biên lợi nhuận", "bien loi nhuan",
                "phân tích doanh thu", "phan tich doanh thu")) {
            return null; // Let AI handle forecast queries for richer analysis
        }

        // "doanh thu" / "revenue" — basic revenue summary
        if (matchesAny(msg, "doanh thu", "doanh so", "revenue")) {
            return buildLocalRevenueReply();
        }

        return null; // Can't answer locally
    }

    // ===== LOCAL REPLY BUILDERS =====

    private String buildLocalProductListReply() {
        try {
            return executor.execute(em -> {
                List<String> categories = productRepository.listCategories(em);
                List<Product> products = productRepository.findAll(em, false);

                if (products.isEmpty()) {
                    return "🛒 **FreshMart** chuyên cung cấp thực phẩm tươi sạch bao gồm:\n" +
                           "- 🥬 Rau củ tươi\n" +
                           "- 🍎 Trái cây theo mùa\n" +
                           "- 🥩 Thịt tươi sống\n" +
                           "- 🐟 Hải sản\n" +
                           "- 🥛 Sữa & đồ uống\n\n" +
                           "Bạn muốn xem giá sản phẩm nào cụ thể? 😊";
                }

                StringBuilder sb = new StringBuilder();
                sb.append("🛒 **FreshMart hiện đang bán ").append(products.size()).append(" sản phẩm** thuộc các nhóm:\n");

                if (!categories.isEmpty()) {
                    for (String cat : categories) {
                        sb.append("- **").append(cat).append("**: ");
                        String items = products.stream()
                                .filter(p -> cat.equals(p.getCategory()))
                                .map(Product::getName)
                                .collect(Collectors.joining(", "));
                        sb.append(items).append("\n");
                    }
                }

                sb.append("\nBạn muốn xem giá sản phẩm nào cụ thể? 😊");
                return sb.toString();
            });
        } catch (Exception e) {
            System.err.println("[ChatContextService] buildLocalProductListReply failed: " + e.getMessage());
            return getStaticProductListReply();
        }
    }

    private String buildLocalPriceReply(String msg) {
        try {
            return executor.execute(em -> {
                List<Product> products = productRepository.findAll(em, false);
                List<Product> matched = products.stream()
                        .filter(p -> msg.contains(p.getName().toLowerCase()))
                        .collect(Collectors.toList());

                if (matched.isEmpty()) {
                    // Show all prices
                    StringBuilder sb = new StringBuilder("💰 **Bảng giá sản phẩm FreshMart:**\n");
                    for (Product p : products) {
                        sb.append("- ").append(p.getName());
                        sb.append(": **").append(formatVND(p.getSellPrice())).append("**");
                        if (p.getUnit() != null && !p.getUnit().isBlank()) {
                            sb.append("/").append(p.getUnit());
                        }
                        sb.append("\n");
                    }
                    return sb.toString();
                }

                StringBuilder sb = new StringBuilder();
                for (Product p : matched) {
                    sb.append("🏷️ **").append(p.getName()).append("**: ");
                    sb.append(formatVND(p.getSellPrice()));
                    if (p.getUnit() != null && !p.getUnit().isBlank()) {
                        sb.append("/").append(p.getUnit());
                    }
                    sb.append("\n");
                }
                return sb.toString();
            });
        } catch (Exception e) {
            System.err.println("[ChatContextService] buildLocalPriceReply failed: " + e.getMessage());
            return "💰 Mình chưa thể tải bảng giá lúc này. Bạn có thể xem giá sản phẩm trực tiếp trên website nhé! 😊";
        }
    }

    private String buildLocalCategoryReply(String msg) {
        try {
            return executor.execute(em -> {
                List<Product> products = productRepository.findAll(em, false);
                String targetCategory = null;

                if (matchesAny(msg, "rau", "rau củ", "rau cu")) targetCategory = "Rau củ";
                else if (matchesAny(msg, "thịt", "thit")) targetCategory = "Thịt";
                else if (matchesAny(msg, "hải sản", "hai san", "cá", "tôm")) targetCategory = "Hải sản";
                else if (matchesAny(msg, "trái cây", "trai cay")) targetCategory = "Trái cây";
                else if (matchesAny(msg, "sữa", "sua", "đồ uống", "do uong", "nước", "nuoc")) targetCategory = "Sữa";

                final String cat = targetCategory;
                List<Product> filtered;

                if (cat != null) {
                    filtered = products.stream()
                            .filter(p -> p.getCategory() != null && p.getCategory().contains(cat))
                            .collect(Collectors.toList());
                } else {
                    // broad match
                    filtered = products.stream()
                            .filter(p -> {
                                String name = p.getName().toLowerCase();
                                return msg.contains(name) || name.contains(extractKeyword(msg));
                            })
                            .collect(Collectors.toList());
                }

                if (filtered.isEmpty()) {
                    return null;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("🥬 **Sản phẩm");
                if (cat != null) sb.append(" ").append(cat);
                sb.append(" tại FreshMart** (").append(filtered.size()).append(" sản phẩm):\n");

                for (Product p : filtered) {
                    sb.append("- ").append(p.getName());
                    sb.append(" — **").append(formatVND(p.getSellPrice())).append("**");
                    if (p.getUnit() != null && !p.getUnit().isBlank()) {
                        sb.append("/").append(p.getUnit());
                    }
                    sb.append("\n");
                }

                return sb.toString();
            });
        } catch (Exception e) {
            System.err.println("[ChatContextService] buildLocalCategoryReply failed: " + e.getMessage());
            return null; // Will fall through to static fallback in ChatbotService
        }
    }

    private String buildLocalBestSellerReply() {
        try {
            return executor.execute(em -> {
                // Find most ordered products
                @SuppressWarnings("unchecked")
                List<Object[]> results = em.createQuery(
                        "SELECT oi.product.name, SUM(oi.quantity) as totalQty " +
                        "FROM OrderItem oi GROUP BY oi.product.name ORDER BY totalQty DESC"
                ).setMaxResults(10).getResultList();

                if (results.isEmpty()) {
                    return "📊 Hiện tại chưa có đủ dữ liệu bán hàng để xác định sản phẩm bán chạy. " +
                           "Bạn có thể xem danh sách sản phẩm bằng cách hỏi 'Bạn bán gì?' nhé! 😊";
                }

                StringBuilder sb = new StringBuilder("🏆 **Top sản phẩm bán chạy tại FreshMart:**\n");
                int rank = 1;
                for (Object[] row : results) {
                    sb.append(rank++).append(". ").append(row[0]);
                    sb.append(" (đã bán ").append(row[1]).append(" đơn vị)\n");
                }

                return sb.toString();
            });
        } catch (Exception e) {
            System.err.println("[ChatContextService] buildLocalBestSellerReply failed: " + e.getMessage());
            return "🏆 **Sản phẩm bán chạy tại FreshMart** thường thuộc các nhóm:\n" +
                   "- 🥬 Rau củ tươi (rau cải, cà chua, khoai tây...)\n" +
                   "- 🍎 Trái cây theo mùa\n" +
                   "- 🥩 Thịt tươi sống\n" +
                   "- 🐟 Hải sản\n\n" +
                   "Bạn muốn xem chi tiết sản phẩm nào? 😊";
        }
    }

    private String buildLocalOrderReply(Long userId) {
        if (userId == null) {
            return "🔐 Bạn cần **đăng nhập** để mình hỗ trợ kiểm tra đơn hàng nhé!";
        }

        try {
            return executor.execute(em -> {
                List<Order> orders = orderRepository.findByCustomerId(em, userId);

                if (orders.isEmpty()) {
                    return "📦 Bạn chưa có đơn hàng nào tại FreshMart.";
                }

                StringBuilder sb = new StringBuilder("📦 **Đơn hàng gần đây của bạn:**\n");
                int showCount = Math.min(orders.size(), 5);

                for (int i = 0; i < showCount; i++) {
                    Order o = orders.get(i);
                    sb.append("- Đơn **#").append(o.getOrderCode()).append("**");
                    if (o.getStatus() != null) {
                        sb.append(" — ").append(translateStatus(o.getStatus().name()));
                    }
                    if (o.getTotalAmount() != null) {
                        sb.append(" (").append(formatVND(o.getTotalAmount())).append(")");
                    }
                    sb.append("\n");
                }

                if (orders.size() > showCount) {
                    sb.append("\n_...và ").append(orders.size() - showCount).append(" đơn khác_");
                }

                return sb.toString();
            });
        } catch (Exception e) {
            System.err.println("[ChatContextService] buildLocalOrderReply failed: " + e.getMessage());
            return "📦 Mình chưa thể tải thông tin đơn hàng lúc này. " +
                   "Bạn có thể kiểm tra đơn hàng tại mục **Đơn hàng của tôi** trên website nhé! 😊";
        }
    }

    private String buildLocalRevenueReply() {
        try {
            return executor.execute(em -> {
                LocalDate today = LocalDate.now();
                LocalDate last30 = today.minusDays(30);
                LocalDate last7 = today.minusDays(7);

                List<RevenueDaily> data = revenueRepo.findBetween(em, last30, today);

                if (data.isEmpty()) {
                    return "📊 Chưa có dữ liệu doanh thu trong 30 ngày gần đây.";
                }

                BigDecimal total30 = data.stream()
                        .map(RevenueDaily::getTotalRevenue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal total7 = data.stream()
                        .filter(r -> !r.getRevenueDate().isBefore(last7))
                        .map(RevenueDaily::getTotalRevenue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal avg = total30.divide(BigDecimal.valueOf(Math.max(1, data.size())), 0, RoundingMode.HALF_UP);

                StringBuilder sb = new StringBuilder("📊 **Tổng quan doanh thu FreshMart:**\n");
                sb.append("- 30 ngày gần nhất: **").append(formatVND(total30)).append("**\n");
                sb.append("- 7 ngày gần nhất: **").append(formatVND(total7)).append("**\n");
                sb.append("- Trung bình/ngày: **").append(formatVND(avg)).append("**\n");
                sb.append("- Số ngày có dữ liệu: ").append(data.size()).append(" ngày");

                return sb.toString();
            });
        } catch (Exception e) {
            System.err.println("[ChatContextService] buildLocalRevenueReply failed: " + e.getMessage());
            return "📊 Mình chưa thể tải dữ liệu doanh thu lúc này. Vui lòng thử lại sau nhé!";
        }
    }

    /**
     * Build enhanced forecast context using multi-source data.
     * Used by PromptBuilderService for REVENUE_FORECAST intent.
     */
    public String buildForecastContext() {
        try {
            AiForecastDataService forecastDataService = new AiForecastDataService();
            return forecastDataService.buildForecastContext("month", null);
        } catch (Exception e) {
            System.err.println("[ChatContextService] buildForecastContext failed: " + e.getMessage());
            return buildRevenueContext(); // Fallback to basic revenue context
        }
    }

    // ===== HELPERS =====

    /**
     * Static fallback when DB is not available for product listing.
     */
    private String getStaticProductListReply() {
        return "🛒 **FreshMart** chuyên cung cấp thực phẩm tươi sạch bao gồm:\n" +
               "- 🥬 Rau củ tươi\n" +
               "- 🍎 Trái cây theo mùa\n" +
               "- 🥩 Thịt tươi sống\n" +
               "- 🐟 Hải sản\n" +
               "- 🥛 Sữa & đồ uống\n\n" +
               "Bạn muốn xem giá sản phẩm nào cụ thể? 😊";
    }

    private boolean matchesAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    private String extractKeyword(String msg) {
        // Remove common question words to find the product keyword
        String clean = msg.replaceAll("(có|gì|không|nào|bao nhiêu|giá|bán|rau|củ|quả)", "").trim();
        return clean.isEmpty() ? msg : clean;
    }

    private String formatVND(BigDecimal amount) {
        if (amount == null) return "N/A";
        return VND_FORMAT.format(amount) + " VND";
    }

    private String translateStatus(String status) {
        if (status == null) return "Không rõ";
        switch (status.toUpperCase()) {
            case "PENDING": return "⏳ Đang chờ xử lý";
            case "PROCESSING": return "🔄 Đang chuẩn bị";
            case "SHIPPING": return "🚚 Đang giao hàng";
            case "COMPLETED": return "✅ Đã hoàn tất";
            case "CANCELED": return "❌ Đã hủy";
            default: return status;
        }
    }
}
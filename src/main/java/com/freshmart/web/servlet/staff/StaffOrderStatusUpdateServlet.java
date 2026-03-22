package com.freshmart.web.servlet.staff;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.enums.OrderStatus;
import com.freshmart.service.OrderService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = { "/staff/orders/update-status" })
public class StaffOrderStatusUpdateServlet extends HttpServlet {

    private static final String SESSION_OMS_SUCCESS = "staffOmsSuccessMessage";
    private static final String SESSION_OMS_ERROR = "staffOmsErrorMessage";

    private final OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rawId = req.getParameter("id");
        String rawStatus = req.getParameter("status");

        try {
            if (rawId == null || rawId.isBlank()) {
                throw new IllegalArgumentException("Thiếu order ID.");
            }

            if (rawStatus == null || rawStatus.isBlank()) {
                throw new IllegalArgumentException("Thiếu trạng thái cần cập nhật.");
            }

            Long orderId = Long.parseLong(rawId.trim());
            OrderStatus targetStatus = OrderStatus.valueOf(rawStatus.trim().toUpperCase());

            if (targetStatus == OrderStatus.COMPLETED) {
                throw new IllegalArgumentException("COMPLETED phải đi qua luồng /staff/orders/complete.");
            }

            User actor = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
            Long actorUserId = actor == null ? null : actor.getId();
            orderService.updateOrderStatus(orderId, targetStatus, actorUserId);

            req.getSession().setAttribute(
                    SESSION_OMS_SUCCESS,
                    "Đơn #" + orderId + " đã chuyển sang trạng thái " + targetStatus + ".");
        } catch (RuntimeException ex) {
            req.getSession().setAttribute(
                    SESSION_OMS_ERROR,
                    "Không thể cập nhật trạng thái đơn: " + ex.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/staff/orders/detail?id=" + rawId);
    }
}

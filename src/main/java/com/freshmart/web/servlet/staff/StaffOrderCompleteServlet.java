package com.freshmart.web.servlet.staff;

import com.freshmart.service.OrderService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/staff/orders/complete"})
public class StaffOrderCompleteServlet extends HttpServlet {

    private static final String SESSION_OMS_SUCCESS = "staffOmsSuccessMessage";
    private static final String SESSION_OMS_ERROR = "staffOmsErrorMessage";

    private final OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rawId = req.getParameter("id");
        try {
            if (rawId == null || rawId.isBlank()) {
                throw new IllegalArgumentException("Thiếu order ID.");
            }

            Long orderId = Long.parseLong(rawId.trim());
            orderService.completeOrder(orderId);
            req.getSession().setAttribute(
                    SESSION_OMS_SUCCESS,
                    "Đơn #" + orderId + " đã được hoàn tất theo FEFO."
            );
        } catch (RuntimeException ex) {
            req.getSession().setAttribute(
                    SESSION_OMS_ERROR,
                    "Không thể hoàn tất đơn: " + ex.getMessage()
            );
        }

        resp.sendRedirect(req.getContextPath() + "/staff/orders/detail?id=" + rawId);
    }
}

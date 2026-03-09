package com.freshmart.web.servlet.staff;

import com.freshmart.service.StaffOrderOmsService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/staff/orders/detail"})
public class StaffOrderDetailServlet extends HttpServlet {

    private static final String SESSION_OMS_SUCCESS = "staffOmsSuccessMessage";
    private static final String SESSION_OMS_ERROR = "staffOmsErrorMessage";

    private final StaffOrderOmsService omsService = new StaffOrderOmsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object successMessage = req.getSession().getAttribute(SESSION_OMS_SUCCESS);
        Object errorMessage = req.getSession().getAttribute(SESSION_OMS_ERROR);

        if (successMessage != null) {
            req.setAttribute("successMessage", successMessage);
            req.getSession().removeAttribute(SESSION_OMS_SUCCESS);
        }
        if (errorMessage != null) {
            req.setAttribute("errorMessage", errorMessage);
            req.getSession().removeAttribute(SESSION_OMS_ERROR);
        }

        try {
            String rawId = req.getParameter("id");
            if (rawId == null || rawId.isBlank()) {
                throw new IllegalArgumentException("Thiếu order ID.");
            }

            Long orderId = Long.parseLong(rawId.trim());
            req.setAttribute("detailView",
                    omsService.getOrderDetail(orderId, StaffOrderOmsService.DEFAULT_NEAR_EXPIRY_DAYS));
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
        }

        req.getRequestDispatcher("/WEB-INF/jsp/staff/order_detail.jsp").forward(req, resp);
    }
}

package com.freshmart.web.servlet.seller;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.Order;
import com.freshmart.entity.User;
import com.freshmart.enums.PaymentMethod;
import com.freshmart.service.OrderService;
import com.freshmart.service.dto.ItemRequest;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/seller/pos/checkout"})
public class SellerCheckoutServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) req.getSession().getAttribute(AppConstants.SESSION_SELLER_CART);

        if (cart == null || cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/seller/pos");
            return;
        }

        User seller = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (seller == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        PaymentMethod method = PaymentMethod.CASH;
        String pm = req.getParameter("paymentMethod");
        try {
            if (pm != null) method = PaymentMethod.valueOf(pm);
        } catch (Exception ignored) {}

        List<ItemRequest> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : cart.entrySet()) {
            items.add(new ItemRequest(e.getKey(), e.getValue()));
        }

        try {
            Order order = orderService.createSellerWalkInOrder(seller.getId(), method, items, true);
            req.getSession().removeAttribute(AppConstants.SESSION_SELLER_CART);

            req.setAttribute("order", order);
            req.getRequestDispatcher("/WEB-INF/jsp/seller/invoice.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/seller/checkout_error.jsp").forward(req, resp);
        }
    }
}

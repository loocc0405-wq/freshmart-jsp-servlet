package com.freshmart.web.servlet.seller;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.enums.Role;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {"/seller/dashboard"})
public class SellerDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User sessionUser = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (sessionUser.getRole() != Role.SELLER && sessionUser.getRole() != Role.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        @SuppressWarnings("unchecked")
        Map<Long, Integer> sellerCart = (Map<Long, Integer>) req.getSession().getAttribute(AppConstants.SESSION_SELLER_CART);

        int skuCount = 0;
        int itemCount = 0;
        if (sellerCart != null) {
            skuCount = sellerCart.size();
            for (Integer qty : sellerCart.values()) {
                if (qty != null) {
                    itemCount += qty;
                }
            }
        }

        req.setAttribute("sellerSkuCount", skuCount);
        req.setAttribute("sellerItemCount", itemCount);
        req.getRequestDispatcher("/WEB-INF/jsp/seller/dashboard.jsp").forward(req, resp);
    }
}

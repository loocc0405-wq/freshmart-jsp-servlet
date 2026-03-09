package com.freshmart.web.servlet.customer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import com.freshmart.repository.CartRepository;
import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/customer/checkout")
public class CustomerCheckoutServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute(AppConstants.SESSION_USER);

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {

            var order = orderService.createCustomerOrder(user.getId());

            resp.sendRedirect(req.getContextPath()
                + "/customer/order-success?id=" + order.getId());

        } catch (Exception e) {

            // ===== DEBUG =====
            e.printStackTrace();

            // gửi message lỗi sang JSP
            req.setAttribute("error", e.getMessage());

            // ===== LOAD LẠI CART ITEMS =====
            EntityManagerFactory emf =
                    Persistence.createEntityManagerFactory("freshmartPU");

            EntityManager em = emf.createEntityManager();

            try {

                CartRepository cartRepo = new CartRepository();

                var items = cartRepo.findItemsByUserId(em, user.getId());

                req.setAttribute("items", items);

            } catch (Exception ex) {

                ex.printStackTrace();
                req.setAttribute("error", "Failed to reload cart items");

            } finally {

                // đóng entity manager
                if (em != null && em.isOpen()) {
                    em.close();
                }

                // đóng factory để tránh leak
                if (emf != null && emf.isOpen()) {
                    emf.close();
                }
            }
            // ===== END LOAD =====

            req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp")
               .forward(req, resp);
        }
    }
}
package com.freshmart.web.servlet.admin;

import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.enums.Tier;
import com.freshmart.repository.UserRepository;
import com.freshmart.util.JpaExecutor;
import com.freshmart.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/sellers/add")
public class AdminAddSellerServlet extends HttpServlet {

    private final JpaExecutor executor = new JpaExecutor();
    private final UserRepository userRepo = new UserRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/admin/add_seller.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String fullName = req.getParameter("fullName");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");

        
        if (username == null || username.trim().isEmpty()
                || password == null || password.length() < 6) {
            req.setAttribute("error", "Username không được rỗng và password tối thiểu 6 ký tự.");
            req.getRequestDispatcher("/WEB-INF/jsp/admin/add_seller.jsp").forward(req, resp);
            return;
        }

        try {
            executor.executeVoid(em -> {
                if (userRepo.existsByUsername(em, username.trim())) {
                    throw new RuntimeException("USERNAME_EXISTS");
                }

                User u = new User();
                u.setUsername(username.trim());
                u.setPasswordHash(PasswordUtil.hash(password));
                u.setRole(Role.SELLER);
                u.setTier(Tier.FREE); // Seller không cần tier PRO
                u.setFullName(fullName);
                u.setPhone(phone);
                u.setAddress(address);
                u.setActive(true);

                userRepo.save(em, u);
            });

            resp.sendRedirect(req.getContextPath() + "/admin/sellers");

        } catch (RuntimeException ex) {
            if ("USERNAME_EXISTS".equals(ex.getMessage())) {
                req.setAttribute("error", "Username đã tồn tại.");
            } else {
                req.setAttribute("error", "Tạo seller thất bại: " + ex.getMessage());
            }
            req.getRequestDispatcher("/WEB-INF/jsp/admin/add_seller.jsp").forward(req, resp);
        }
    }
}
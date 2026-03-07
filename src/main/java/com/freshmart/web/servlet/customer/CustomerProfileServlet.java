package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.CustomerProfileService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/customer/profile"})
public class CustomerProfileServlet extends HttpServlet {

    private final CustomerProfileService customerProfileService = new CustomerProfileService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User sessionUser = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            User user = customerProfileService.getById(sessionUser.getId());
            req.setAttribute("profileUser", user);
            req.getRequestDispatcher("/WEB-INF/jsp/customer/profile.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/common/403.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User sessionUser = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String fullName = req.getParameter("fullName");
        String gender = req.getParameter("gender");
        String dob = req.getParameter("dob");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");

        try {
            User updated = customerProfileService.updateProfile(
                    sessionUser.getId(),
                    fullName,
                    gender,
                    dob,
                    phone,
                    address
            );

            req.getSession().setAttribute(AppConstants.SESSION_USER, updated);
            req.setAttribute("profileUser", updated);
            req.setAttribute("successMessage", "Cập nhật hồ sơ thành công.");
            req.getRequestDispatcher("/WEB-INF/jsp/customer/profile.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            req.setAttribute("profileUser", sessionUser);
            req.setAttribute("errorMessage", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/customer/profile.jsp").forward(req, resp);
        }
    }
}
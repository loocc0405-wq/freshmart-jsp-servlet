package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.RegistrationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private final RegistrationService registrationService = new RegistrationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);

        if (currentUser != null) {
            resp.sendRedirect(req.getContextPath() + "/customer/dashboard");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String fullName       = req.getParameter("fullName");
        String username       = req.getParameter("username");
        String email          = req.getParameter("email");
        String phone          = req.getParameter("phone");
        String password       = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String gender         = req.getParameter("gender");
        String dob            = req.getParameter("dob");
        String address        = req.getParameter("address");
        String agreeTerms     = req.getParameter("agreeTerms");

        // honeypot – bots fill this, humans don't
        String website = req.getParameter("website");
        if (website != null && !website.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/register");
            return;
        }

        Map<String, String> errors = new LinkedHashMap<>();
        errors.putAll(registrationService.validate(
                fullName, username, email, phone,
                password, confirmPassword,
                gender, dob, address, agreeTerms
        ));

        if (errors.isEmpty()) {
            errors.putAll(registrationService.validateBusinessRules(username, email));
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("formData", buildFormData(req));
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
            return;
        }

        try {
            registrationService.registerCustomer(
                    fullName, username, email, phone, password, gender, dob, address
            );
            resp.sendRedirect(req.getContextPath() + "/login?registered=1");
        } catch (RuntimeException ex) {
            mapRegistrationError(errors, ex);
            req.setAttribute("errors", errors);
            req.setAttribute("formData", buildFormData(req));
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
        }
    }

    private Map<String, String> buildFormData(HttpServletRequest req) {
        Map<String, String> formData = new LinkedHashMap<>();
        formData.put("fullName",   req.getParameter("fullName"));
        formData.put("username",   req.getParameter("username"));
        formData.put("email",      req.getParameter("email"));
        formData.put("phone",      req.getParameter("phone"));
        formData.put("gender",     req.getParameter("gender"));
        formData.put("dob",        req.getParameter("dob"));
        formData.put("address",    req.getParameter("address"));
        formData.put("agreeTerms", req.getParameter("agreeTerms"));
        return formData;
    }

    private void mapRegistrationError(Map<String, String> errors, RuntimeException ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage();

        if (containsIgnoreCase(msg, "username")) {
            errors.put("username", "Username đã tồn tại.");
            return;
        }
        if (containsIgnoreCase(msg, "email")) {
            errors.put("email", "Email đã được sử dụng.");
            return;
        }
        errors.put("general", msg.isBlank() ? "Đăng ký thất bại. Vui lòng thử lại." : msg);
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source != null && source.toLowerCase().contains(target.toLowerCase());
    }
}

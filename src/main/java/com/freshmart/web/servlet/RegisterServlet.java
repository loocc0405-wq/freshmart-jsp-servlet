package com.freshmart.web.servlet;

import com.freshmart.service.RegistrationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private final RegistrationService registrationService = new RegistrationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String fullName = req.getParameter("fullName");
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String gender = req.getParameter("gender");
        String dob = req.getParameter("dob");
        String address = req.getParameter("address");
        String agreeTerms = req.getParameter("agreeTerms");

        Map<String, String> errors = new LinkedHashMap<>();
        errors.putAll(registrationService.validate(
                fullName,
                username,
                email,
                phone,
                password,
                confirmPassword,
                gender,
                dob,
                address,
                agreeTerms
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
        } catch (IllegalArgumentException ex) {
            errors.put("general", ex.getMessage());
            req.setAttribute("errors", errors);
            req.setAttribute("formData", buildFormData(req));
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
        }
    }

    private Map<String, String> buildFormData(HttpServletRequest req) {
        Map<String, String> formData = new LinkedHashMap<>();
        formData.put("fullName", req.getParameter("fullName"));
        formData.put("username", req.getParameter("username"));
        formData.put("email", req.getParameter("email"));
        formData.put("phone", req.getParameter("phone"));
        formData.put("gender", req.getParameter("gender"));
        formData.put("dob", req.getParameter("dob"));
        formData.put("address", req.getParameter("address"));
        return formData;
    }
}

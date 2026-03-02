package com.freshmart.web.servlet.admin;

import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.repository.UserRepository;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/sellers/toggle")
public class AdminToggleSellerActiveServlet extends HttpServlet {

    private final JpaExecutor executor = new JpaExecutor();
    private final UserRepository userRepo = new UserRepository();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = Long.parseLong(req.getParameter("id"));

        executor.executeVoid(em -> {
            User u = userRepo.findById(em, id).orElseThrow();
            if (u.getRole() != Role.SELLER) throw new RuntimeException("NOT_SELLER");
            u.setActive(!u.isActive());
            userRepo.save(em, u);
        });

        resp.sendRedirect(req.getContextPath() + "/admin/sellers");
    }
}
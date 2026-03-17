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
        String idRaw = req.getParameter("id");
        if (idRaw == null || idRaw.isBlank()) {
            req.getSession().setAttribute("sellerActionError", "Thiếu ID seller.");
            resp.sendRedirect(req.getContextPath() + "/admin/sellers");
            return;
        }

        try {
            Long id = Long.parseLong(idRaw);
            String newStatus = executor.execute(em -> {
                User u = userRepo.findById(em, id).orElseThrow(() -> new RuntimeException("Không tìm thấy seller."));
                if (u.getRole() != Role.SELLER) throw new RuntimeException("Tài khoản không phải là Seller.");
                
                boolean targetStatus = !u.isActive();
                u.setActive(targetStatus);
                userRepo.save(em, u);
                return targetStatus ? "kích hoạt" : "khoán (lock)";
            });

            req.getSession().setAttribute("sellerActionSuccess", "Đã " + newStatus + " seller thành công.");
        } catch (Exception ex) {
            req.getSession().setAttribute("sellerActionError", "Lỗi: " + ex.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/sellers");
    }
}
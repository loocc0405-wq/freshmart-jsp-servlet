package com.freshmart.web.servlet.admin;

import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.repository.UserRepository;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/sellers")
public class AdminSellerListServlet extends HttpServlet {

    private final JpaExecutor executor = new JpaExecutor();
    private final UserRepository userRepo = new UserRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<User> sellers = executor.execute(em -> userRepo.findByRole(em, Role.SELLER));
        req.setAttribute("sellers", sellers);

        req.getRequestDispatcher("/WEB-INF/jsp/admin/seller_list.jsp").forward(req, resp);
    }
}
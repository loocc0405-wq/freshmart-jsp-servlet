package com.freshmart.web.servlet.admin;

import com.freshmart.service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/admin/delete-product")
public class AdminDeleteProductServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long id = Long.parseLong(req.getParameter("id"));
        productService.deleteById(id);

        resp.sendRedirect(req.getContextPath() + "/admin/products");
    }
}
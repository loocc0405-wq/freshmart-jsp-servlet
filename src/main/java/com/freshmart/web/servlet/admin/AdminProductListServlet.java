package com.freshmart.web.servlet.admin;

import com.freshmart.service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/admin/products")
public class AdminProductListServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("products", productService.listAll());
        req.getRequestDispatcher("/WEB-INF/jsp/admin/product_list.jsp")
           .forward(req, resp);
    }
}
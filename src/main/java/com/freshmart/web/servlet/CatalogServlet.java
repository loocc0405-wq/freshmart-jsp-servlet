package com.freshmart.web.servlet;

import com.freshmart.entity.Product;
import com.freshmart.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/catalog"})
public class CatalogServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = req.getParameter("q");
        String cat = req.getParameter("category");

        List<Product> products = (q != null || cat != null)
                ? productService.search(q, cat)
                : productService.listAll();

        req.setAttribute("products", products);
        req.getRequestDispatcher("/WEB-INF/jsp/catalog/catalog.jsp").forward(req, resp);
    }
}

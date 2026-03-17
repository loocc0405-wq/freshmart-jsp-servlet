package com.freshmart.web.servlet.admin;

import com.freshmart.entity.Product;
import com.freshmart.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/admin/add-product")
public class AdminAddProductServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher("/WEB-INF/jsp/admin/add_product.jsp")
           .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            String name = req.getParameter("name");
            String category = req.getParameter("category");
            String unit = req.getParameter("unit");
            String description = req.getParameter("description");
            String imageUrl = req.getParameter("imageUrl");

            BigDecimal sellPrice =
                    new BigDecimal(req.getParameter("sellPrice"));

            Product product = new Product();
            product.setName(name);
            product.setCategory(category);
            product.setUnit(unit);
            product.setDescription(description);
            product.setImageUrl(imageUrl);
            product.setSellPrice(sellPrice);

            productService.save(product); // ✅ đúng kiến trúc

            resp.sendRedirect(req.getContextPath() + "/admin");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Thêm sản phẩm thất bại!");
            req.getRequestDispatcher("/WEB-INF/jsp/admin/add_product.jsp")
               .forward(req, resp);
        }
    }
}
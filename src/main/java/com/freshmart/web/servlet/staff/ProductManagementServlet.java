package com.freshmart.web.servlet.staff;

import com.freshmart.entity.Product;
import com.freshmart.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/staff/products")
public class ProductManagementServlet extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() {
        productService = new ProductService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            listProducts(request, response);
        } else {
            switch (action) {
                case "add":
                    showForm(request, response, null);
                    break;

                case "edit":
                    Long id = Long.parseLong(request.getParameter("id"));
                    Product product = productService.getById(id);
                    showForm(request, response, product);
                    break;

                case "delete":
                    deleteProduct(request, response);
                    break;

                default:
                    listProducts(request, response);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String category = request.getParameter("category");
        String unit = request.getParameter("unit");
        String priceStr = request.getParameter("sellPrice");
        String imageUrl = request.getParameter("imageUrl");
        String description = request.getParameter("description");

        Product product;

        if (idStr == null || idStr.isEmpty()) {
            product = new Product();
        } else {
            Long id = Long.parseLong(idStr);
            product = productService.getById(id);
        }

        product.setName(name);
        product.setCategory(category);
        product.setUnit(unit);

        if (priceStr != null && !priceStr.isEmpty()) {
            product.setSellPrice(new BigDecimal(priceStr));
        }

        product.setImageUrl(imageUrl);
        product.setDescription(description);

        productService.save(product);

        response.sendRedirect(request.getContextPath() + "/staff/products");
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Product> products = productService.listAll();
        request.setAttribute("products", products);

        request.getRequestDispatcher("/WEB-INF/jsp/staff/product_list.jsp")
                .forward(request, response);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Product product)
            throws ServletException, IOException {

        request.setAttribute("product", product);

        request.getRequestDispatcher("/WEB-INF/jsp/staff/product_form.jsp")
                .forward(request, response);
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Long id = Long.parseLong(request.getParameter("id"));
        productService.deleteById(id);

        response.sendRedirect(request.getContextPath() + "/staff/products");
    }
}
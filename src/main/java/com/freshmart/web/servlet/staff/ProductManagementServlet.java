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

        if (action == null || action.isBlank()) {
            listProducts(request, response);
            return;
        }

        switch (action) {
            case "add":
                showForm(request, response, null);
                break;

            case "edit": {
                String idStr = request.getParameter("id");
                if (idStr == null || idStr.isBlank()) {
                    request.getSession().setAttribute("flash", "Missing product id.");
                    response.sendRedirect(request.getContextPath() + "/staff/products");
                    return;
                }
                Long id = Long.parseLong(idStr);
                Product product = productService.getById(id);
                if (product == null) {
                    request.getSession().setAttribute("flash", "Product not found.");
                    response.sendRedirect(request.getContextPath() + "/staff/products");
                    return;
                }
                showForm(request, response, product);
                break;
            }

            default:
                listProducts(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        // DELETE bằng POST
        if ("delete".equalsIgnoreCase(action)) {
            deleteProductPost(request, response);
            return;
        }

        // SAVE (create/update)
        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String category = request.getParameter("category");
        String unit = request.getParameter("unit");
        String priceStr = request.getParameter("sellPrice");
        String imageUrl = request.getParameter("imageUrl");
        String description = request.getParameter("description");

        Product product;
        boolean isCreate = (idStr == null || idStr.isBlank());

        if (isCreate) {
            product = new Product();
        } else {
            Long id = Long.parseLong(idStr);
            product = productService.getById(id);
            if (product == null) {
                request.getSession().setAttribute("flash", "Product not found.");
                response.sendRedirect(request.getContextPath() + "/staff/products");
                return;
            }
        }

        product.setName(name);
        product.setCategory(category);
        product.setUnit(unit);

        // parse sellPrice an toàn
        if (priceStr != null && !priceStr.isBlank()) {
            try {
                product.setSellPrice(new BigDecimal(priceStr.trim()));
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("flash", "Invalid sell price.");
                // quay lại form (giữ dữ liệu)
                request.setAttribute("product", product);
                request.getRequestDispatcher("/WEB-INF/jsp/staff/product_form.jsp").forward(request, response);
                return;
            }
        }

        product.setImageUrl(imageUrl);
        product.setDescription(description);

        productService.save(product);

        request.getSession().setAttribute("flash",
                isCreate ? "Product created successfully!" : "Product updated successfully!");
        response.sendRedirect(request.getContextPath() + "/staff/products");
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String category = request.getParameter("category");

        if (keyword != null) keyword = keyword.trim();
        if (category != null) category = category.trim();

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();

        List<Product> products = (hasKeyword || hasCategory)
                ? productService.search(keyword, category)
                : productService.listAll();

        request.setAttribute("products", products);

        // để JSP set lại ô tìm kiếm
        request.setAttribute("keyword", keyword);
        request.setAttribute("category", category);

        request.getRequestDispatcher("/WEB-INF/jsp/staff/product_list.jsp")
                .forward(request, response);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Product product)
            throws ServletException, IOException {

        request.setAttribute("product", product);
        request.getRequestDispatcher("/WEB-INF/jsp/staff/product_form.jsp")
                .forward(request, response);
    }

    private void deleteProductPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            request.getSession().setAttribute("flash", "Missing product id.");
            response.sendRedirect(request.getContextPath() + "/staff/products");
            return;
        }

        Long id = Long.parseLong(idStr);
        productService.deleteById(id);

        request.getSession().setAttribute("flash", "Product deleted successfully!");
        response.sendRedirect(request.getContextPath() + "/staff/products");
    }
}
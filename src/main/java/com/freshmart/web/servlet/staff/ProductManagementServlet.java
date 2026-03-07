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
        String activeParam = request.getParameter("active");

        // Trim data
        if (name != null) name = name.trim();
        if (category != null) category = category.trim();
        if (unit != null) unit = unit.trim();
        if (imageUrl != null) imageUrl = imageUrl.trim();
        if (description != null) description = description.trim();
        boolean active = "on".equalsIgnoreCase(activeParam) || "true".equalsIgnoreCase(activeParam);

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

        // Validation
        StringBuilder errors = new StringBuilder();

        if (name == null || name.isEmpty()) {
            errors.append("Product name is required. ");
        }

        if (category == null || category.isEmpty()) {
            errors.append("Category is required. ");
        }

        if (unit == null || unit.isEmpty()) {
            errors.append("Unit is required. ");
        }

        if (priceStr != null && !priceStr.trim().isEmpty()) {
            try {
                BigDecimal price = new BigDecimal(priceStr.trim());
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    errors.append("Sell price cannot be negative. ");
                }
            } catch (NumberFormatException e) {
                errors.append("Invalid sell price format. ");
            }
        } else {
            errors.append("Sell price is required. ");
        }

        // validate imageUrl if provided
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                new java.net.URL(imageUrl);
            } catch (Exception e) {
                errors.append("Image URL is not a valid URL. ");
            }
        }

        // If validation fails, forward back to form with errors
        if (errors.length() > 0) {
            request.setAttribute("product", product);
            request.setAttribute("errors", errors.toString().trim());
            // Keep user input
            product.setName(name);
            product.setCategory(category);
            product.setUnit(unit);
            product.setImageUrl(imageUrl);
            product.setDescription(description);
            product.setActive(active);
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                try {
                    product.setSellPrice(new BigDecimal(priceStr.trim()));
                } catch (NumberFormatException e) {
                    product.setSellPrice(BigDecimal.ZERO);
                }
            }
            request.getRequestDispatcher("/WEB-INF/jsp/staff/product_form.jsp").forward(request, response);
            return;
        }

        // Set validated data
        product.setName(name);
        product.setCategory(category);
        product.setUnit(unit);

        // parse sellPrice an toàn
        if (priceStr != null && !priceStr.isBlank()) {
            product.setSellPrice(new BigDecimal(priceStr.trim()));
        }

        product.setImageUrl(imageUrl);
        product.setDescription(description);
        product.setActive(active);

        productService.save(product);

        request.getSession().setAttribute("flash",
                isCreate ? "Product created successfully!" : "Product updated successfully!");
        response.sendRedirect(request.getContextPath() + "/staff/products");
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String category = request.getParameter("category");
        String showInactiveParam = request.getParameter("showInactive");

        if (keyword != null) keyword = keyword.trim();
        if (category != null) category = category.trim();

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();
        boolean showInactive = "on".equalsIgnoreCase(showInactiveParam) || "true".equalsIgnoreCase(showInactiveParam);

        List<Product> products = (hasKeyword || hasCategory)
                ? productService.search(keyword, category, showInactive)
                : productService.listAll(showInactive);

        request.setAttribute("showInactive", showInactive);

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
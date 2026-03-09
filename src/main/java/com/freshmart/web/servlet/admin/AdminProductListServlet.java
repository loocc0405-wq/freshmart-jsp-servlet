package com.freshmart.web.servlet.admin;

import com.freshmart.entity.Product;
import com.freshmart.service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/products")
public class AdminProductListServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String keyword = req.getParameter("keyword");
        String category = req.getParameter("category");
        String showInactiveParam = req.getParameter("showInactive");
        String pageParam = req.getParameter("page");

        if (keyword != null) keyword = keyword.trim();
        if (category != null) category = category.trim();

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();
        boolean showInactive = "on".equalsIgnoreCase(showInactiveParam) || "true".equalsIgnoreCase(showInactiveParam);

        // Pagination parameters
        int pageSize = 10;
        int currentPage = 1;
        
        if (pageParam != null && !pageParam.isBlank()) {
            try {
                currentPage = Integer.parseInt(pageParam);
                if (currentPage < 1) currentPage = 1;
            } catch (NumberFormatException e) {
                currentPage = 1;
            }
        }

        // Get total count
        long totalItems = (hasKeyword || hasCategory)
                ? productService.countSearch(keyword, category, showInactive)
                : productService.countAll(showInactive);

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages < 1) totalPages = 1;

        // Handle edge case: currentPage exceeds totalPages
        if (currentPage > totalPages && totalItems > 0) {
            currentPage = totalPages;
        }

        // Get paginated products
        List<Product> products = (hasKeyword || hasCategory)
                ? productService.searchPaginated(keyword, category, showInactive, currentPage, pageSize)
                : productService.listAllPaginated(showInactive, currentPage, pageSize);

        // Set attributes for JSP
        req.setAttribute("products", products);
        req.setAttribute("keyword", keyword);
        req.setAttribute("category", category);
        req.setAttribute("showInactive", showInactive);
        
        // Pagination attributes
        req.setAttribute("currentPage", currentPage);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("totalItems", totalItems);
        req.setAttribute("totalPages", totalPages);

        req.getRequestDispatcher("/WEB-INF/jsp/admin/product_list.jsp")
           .forward(req, resp);
    }
}
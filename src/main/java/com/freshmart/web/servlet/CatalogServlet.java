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
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/catalog"})
public class CatalogServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = req.getParameter("q");
        String cat = req.getParameter("category");

        boolean hasFilter = (q != null && !q.isBlank()) || (cat != null && !cat.isBlank());

        List<Product> products = hasFilter
                ? productService.search(q, cat)
                : productService.listAll();

        // NEW: đổ danh sách category cho dropdown
        List<String> categories = productService.listCategories();
        req.setAttribute("categories", categories);

        // NEW: nếu không lọc thì group theo category để hiển thị theo từng mục
        if (!hasFilter) {
            Map<String, List<Product>> grouped = new LinkedHashMap<>();
            for (String c : categories) {
                List<Product> inCat = products.stream()
                        .filter(p -> c.equals(p.getCategory()))
                        .collect(Collectors.toList());
                if (!inCat.isEmpty()) grouped.put(c, inCat);
            }

            List<Product> others = products.stream()
                    .filter(p -> p.getCategory() == null || p.getCategory().isBlank())
                    .collect(Collectors.toList());
            if (!others.isEmpty()) grouped.put("Khác", others);

            req.setAttribute("groupedProducts", grouped);
        }

        req.setAttribute("products", products);
        req.getRequestDispatcher("/WEB-INF/jsp/catalog/catalog.jsp").forward(req, resp);
    }
}
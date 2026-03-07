package com.freshmart.web.servlet;

import com.freshmart.entity.Product;
import com.freshmart.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/catalog"})
public class CatalogServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    // helper for null-safe string comparison
    private static int compareString(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareToIgnoreCase(b);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = req.getParameter("q");
        String cat = req.getParameter("category");
        String sort = req.getParameter("sort");
        String pageParam = req.getParameter("page");

        // normalize inputs
        if (q != null) q = q.trim();
        if (cat != null) cat = cat.trim();
        if (sort != null) sort = sort.trim();

        boolean hasFilter =
                (q != null && !q.isBlank()) ||
                (cat != null && !cat.isBlank());

        List<Product> products = hasFilter
                ? productService.search(q, cat, false)
                : productService.listAll(false);

        // apply sort in memory (list is fairly small)
        if (sort != null) {
            switch (sort) {
                case "name_asc":
                    products.sort((a, b) -> compareString(a.getName(), b.getName()));
                    break;
                case "name_desc":
                    products.sort((a, b) -> compareString(b.getName(), a.getName()));
                    break;
                case "price_asc":
                    products.sort((a, b) -> a.getSellPrice().compareTo(b.getSellPrice()));
                    break;
                case "price_desc":
                    products.sort((a, b) -> b.getSellPrice().compareTo(a.getSellPrice()));
                    break;
                default:
                    // leave default order (id asc from query)
            }
        }

        // pagination
        int pageSize = 12;
        int currentPage = 1;
        if (pageParam != null) {
            try {
                currentPage = Integer.parseInt(pageParam);
            } catch (NumberFormatException ignore) {
            }
        }
        if (currentPage < 1) currentPage = 1;
        int total = products.size();
        int totalPages = total == 0 ? 1 : ((total + pageSize - 1) / pageSize);
        if (currentPage > totalPages) currentPage = totalPages;
        int fromIndex = (currentPage - 1) * pageSize;
        if (fromIndex > total) fromIndex = 0;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Product> pageItems = products.subList(fromIndex, toIndex);

        // dropdown categories
        List<String> categories = productService.listCategories();
        req.setAttribute("categories", categories);

        // group mode (chỉ khi không filter, không sort và trang đầu)
        boolean groupMode = !hasFilter && (sort == null || sort.isBlank()) && currentPage == 1 && categories != null && !categories.isEmpty();
        req.setAttribute("groupMode", groupMode);

        if (groupMode) {
            Map<String, List<Product>> grouped = new LinkedHashMap<>();
            for (String c : categories) {
                List<Product> inCat = products.stream()
                        .filter(p -> c != null && c.equals(p.getCategory()))
                        .collect(Collectors.toList());
                if (!inCat.isEmpty()) grouped.put(c, inCat);
            }

            List<Product> others = products.stream()
                    .filter(p -> p.getCategory() == null || p.getCategory().isBlank())
                    .collect(Collectors.toList());
            if (!others.isEmpty()) grouped.put("Khác", others);

            req.setAttribute("groupedProducts", grouped);
        }

        req.setAttribute("products", pageItems);
        req.setAttribute("currentPage", currentPage);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("sort", sort);
        req.getRequestDispatcher("/WEB-INF/jsp/catalog/catalog.jsp").forward(req, resp);
    }
}
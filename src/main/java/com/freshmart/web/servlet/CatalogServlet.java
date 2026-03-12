package com.freshmart.web.servlet;

import com.freshmart.entity.Product;
import com.freshmart.service.InventoryService;
import com.freshmart.service.ProductService;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/catalog"})
public class CatalogServlet extends HttpServlet {

    private static final List<String> LANDING_CATEGORY_ORDER = Arrays.asList(
            "Rau củ",
            "Trái cây",
            "Thịt",
            "Hải sản",
            "Thực phẩm chế biến sẵn"
    );

    private final ProductService productService = new ProductService();
    private final InventoryService inventoryService = new InventoryService();
    private final JpaExecutor executor = new JpaExecutor();

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
        String stockStatus = req.getParameter("stockStatus");
        String pageParam = req.getParameter("page");

        if (q != null) q = q.trim();
        if (cat != null) cat = cat.trim();
        if (sort != null) sort = sort.trim();
        if (stockStatus != null) stockStatus = stockStatus.trim();

        boolean hasFilter =
                (q != null && !q.isBlank()) ||
                (cat != null && !cat.isBlank()) ||
                (stockStatus != null && !stockStatus.isBlank() && !"all".equals(stockStatus));

        List<Product> products = hasFilter
                ? productService.search(q, cat, stockStatus, false)
                : productService.listAll(false);

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
                    // keep default order
            }
        }

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

        LocalDate today = LocalDate.now();
        Map<Long, Integer> availableQtyMap = new HashMap<>();
        for (Product p : products) {
            int qty = executor.execute(em -> inventoryService.getAvailableQty(em, p.getId(), today));
            availableQtyMap.put(p.getId(), qty);
        }
        req.setAttribute("availableQtyMap", availableQtyMap);

        List<String> categories = orderCategories(productService.listCategories());
        req.setAttribute("categories", categories);

        Map<String, List<Product>> grouped = groupProducts(products, categories);
        boolean landingMode = !hasFilter && (sort == null || sort.isBlank()) && currentPage == 1 && !grouped.isEmpty();

        Map<String, Product> categoryLeadProducts = new LinkedHashMap<>();
        for (Map.Entry<String, List<Product>> entry : grouped.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                categoryLeadProducts.put(entry.getKey(), entry.getValue().get(0));
            }
        }

        List<String> landingCategories = new ArrayList<>(categoryLeadProducts.keySet());
        if (landingCategories.size() > 5) {
            landingCategories = new ArrayList<>(landingCategories.subList(0, 5));
        }

        List<Product> dealProducts = pickProductsByCategory(grouped,
                Arrays.asList("Rau củ", "Trái cây", "Thịt"),
                3);
        List<Product> bestProducts = pickRoundRobin(grouped, 10);

        req.setAttribute("groupMode", landingMode);
        req.setAttribute("landingMode", landingMode);
        req.setAttribute("groupedProducts", grouped);
        req.setAttribute("categoryLeadProducts", categoryLeadProducts);
        req.setAttribute("landingCategories", landingCategories);
        req.setAttribute("dealProducts", dealProducts);
        req.setAttribute("bestProducts", bestProducts);
        req.setAttribute("comparePriceMap", buildComparePriceMap(dealProducts, new BigDecimal("1.33")));
        req.setAttribute("products", pageItems);
        req.setAttribute("resultCount", total);
        req.setAttribute("currentPage", currentPage);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("sort", sort);
        req.setAttribute("stockStatus", stockStatus);

        req.getRequestDispatcher("/WEB-INF/jsp/catalog/catalog.jsp").forward(req, resp);
    }

    private List<String> orderCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        List<String> ordered = new ArrayList<>(categories);
        ordered.sort((a, b) -> {
            int ia = LANDING_CATEGORY_ORDER.indexOf(a);
            int ib = LANDING_CATEGORY_ORDER.indexOf(b);
            ia = ia < 0 ? Integer.MAX_VALUE : ia;
            ib = ib < 0 ? Integer.MAX_VALUE : ib;
            if (ia != ib) return Integer.compare(ia, ib);
            return compareString(a, b);
        });
        return ordered;
    }

    private Map<String, List<Product>> groupProducts(List<Product> products, List<String> orderedCategories) {
        Map<String, List<Product>> grouped = new LinkedHashMap<>();
        if (orderedCategories != null) {
            for (String category : orderedCategories) {
                List<Product> inCategory = products.stream()
                        .filter(p -> category != null && category.equals(p.getCategory()))
                        .collect(Collectors.toList());
                if (!inCategory.isEmpty()) {
                    grouped.put(category, inCategory);
                }
            }
        }

        Set<String> seen = new LinkedHashSet<>(grouped.keySet());
        for (Product product : products) {
            String category = product.getCategory();
            if (category != null && !category.isBlank() && !seen.contains(category)) {
                grouped.put(category, products.stream()
                        .filter(p -> category.equals(p.getCategory()))
                        .collect(Collectors.toList()));
                seen.add(category);
            }
        }

        List<Product> others = products.stream()
                .filter(p -> p.getCategory() == null || p.getCategory().isBlank())
                .collect(Collectors.toList());
        if (!others.isEmpty()) {
            grouped.put("Khác", others);
        }

        return grouped;
    }

    private List<Product> pickProductsByCategory(Map<String, List<Product>> grouped,
                                                 List<String> preferredCategories,
                                                 int maxItems) {
        List<Product> picked = new ArrayList<>();
        Set<Long> seenIds = new LinkedHashSet<>();

        for (String category : preferredCategories) {
            List<Product> list = grouped.get(category);
            if (list == null || list.isEmpty()) {
                continue;
            }
            Product candidate = list.get(0);
            if (candidate != null && seenIds.add(candidate.getId())) {
                picked.add(candidate);
            }
            if (picked.size() >= maxItems) {
                return picked;
            }
        }

        for (List<Product> list : grouped.values()) {
            for (Product product : list) {
                if (seenIds.add(product.getId())) {
                    picked.add(product);
                }
                if (picked.size() >= maxItems) {
                    return picked;
                }
            }
        }
        return picked;
    }

    private List<Product> pickRoundRobin(Map<String, List<Product>> grouped, int maxItems) {
        List<Product> selected = new ArrayList<>();
        if (grouped == null || grouped.isEmpty()) {
            return selected;
        }

        int depth = 0;
        while (selected.size() < maxItems) {
            boolean addedAtLeastOne = false;
            for (List<Product> list : grouped.values()) {
                if (depth < list.size()) {
                    selected.add(list.get(depth));
                    addedAtLeastOne = true;
                    if (selected.size() >= maxItems) {
                        return selected;
                    }
                }
            }
            if (!addedAtLeastOne) {
                break;
            }
            depth++;
        }
        return selected;
    }

    private Map<Long, BigDecimal> buildComparePriceMap(List<Product> products, BigDecimal multiplier) {
        Map<Long, BigDecimal> comparePrices = new HashMap<>();
        for (Product product : products) {
            if (product == null || product.getSellPrice() == null) {
                continue;
            }
            comparePrices.put(
                    product.getId(),
                    product.getSellPrice().multiply(multiplier).setScale(0, RoundingMode.HALF_UP)
            );
        }
        return comparePrices;
    }
}
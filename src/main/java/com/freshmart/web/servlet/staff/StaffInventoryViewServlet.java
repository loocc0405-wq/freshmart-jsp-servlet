package com.freshmart.web.servlet.staff;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.entity.Supplier;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.SupplierRepository;
import com.freshmart.service.ProductLotService;
import com.freshmart.service.dto.InventoryLotFilter;
import com.freshmart.service.dto.StockSummaryDto;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet để xem tồn kho theo lô và FEFO.
 * Map: /staff/inventory
 */
@WebServlet(urlPatterns = {"/staff/inventory", "/staff/inventory/view"})
public class StaffInventoryViewServlet extends HttpServlet {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();
    private final ProductLotRepository lotRepo = new ProductLotRepository();
    private final SupplierRepository supplierRepo = new SupplierRepository();
    private final ProductLotService lotService = new ProductLotService();

    private Long parseLongOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Long.parseLong(raw.trim());
    }

    private Integer parseIntOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Integer.parseInt(raw.trim());
    }

    private LocalDate parseDateOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocalDate.parse(raw.trim());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Object successMessage = req.getSession().getAttribute("successMessage");
            Object errorFlash = req.getSession().getAttribute("errorMessage");
            if (successMessage != null) {
                req.setAttribute("successMessage", successMessage);
                req.getSession().removeAttribute("successMessage");
            }
            if (errorFlash != null) {
                req.setAttribute("errorMessage", errorFlash);
                req.getSession().removeAttribute("errorMessage");
            }

            LocalDate today = LocalDate.now();
            req.setAttribute("today", today);

            List<Product> products = executor.execute(em -> productRepo.findAll(em, true));
            List<Supplier> suppliers = executor.execute(supplierRepo::findAll);
            req.setAttribute("products", products);
            req.setAttribute("suppliers", suppliers);

            InventoryLotFilter filter = new InventoryLotFilter();
            filter.setProductId(parseLongOrNull(req.getParameter("productId")));
            filter.setSupplierId(parseLongOrNull(req.getParameter("supplierId")));
            filter.setStatus(req.getParameter("status"));
            filter.setImportFrom(parseDateOrNull(req.getParameter("importFrom")));
            filter.setImportTo(parseDateOrNull(req.getParameter("importTo")));
            filter.setExpiryFrom(parseDateOrNull(req.getParameter("expiryFrom")));
            filter.setExpiryTo(parseDateOrNull(req.getParameter("expiryTo")));
            filter.setMinQtyLeft(parseIntOrNull(req.getParameter("minQtyLeft")));
            filter.setMaxQtyLeft(parseIntOrNull(req.getParameter("maxQtyLeft")));
            req.setAttribute("filter", filter);

            validateFilter(filter);

            boolean hasFilter = hasFilter(filter);
            if (hasFilter) {
                List<ProductLot> filteredLots = executor.execute(em -> lotRepo.searchLots(em, filter, today));
                long filteredCount = executor.execute(em -> lotRepo.countLots(em, filter, today));
                req.setAttribute("filteredLots", filteredLots);
                req.setAttribute("filteredCount", filteredCount);
            }

            if (filter.getProductId() != null) {
                loadSelectedProductDetail(req, filter.getProductId(), today);
            }
        } catch (Exception ex) {
            req.setAttribute("errorMessage", ex.getMessage());
        }

        req.getRequestDispatcher("/WEB-INF/jsp/staff/inventory_view.jsp").forward(req, resp);
    }

    private void validateFilter(InventoryLotFilter filter) {
        if (filter.getImportFrom() != null && filter.getImportTo() != null
                && filter.getImportFrom().isAfter(filter.getImportTo())) {
            throw new IllegalArgumentException("Ngày nhập bắt đầu không được lớn hơn ngày nhập kết thúc");
        }

        if (filter.getExpiryFrom() != null && filter.getExpiryTo() != null
                && filter.getExpiryFrom().isAfter(filter.getExpiryTo())) {
            throw new IllegalArgumentException("Hạn sử dụng bắt đầu không được lớn hơn hạn sử dụng kết thúc");
        }

        if (filter.getMinQtyLeft() != null && filter.getMaxQtyLeft() != null
                && filter.getMinQtyLeft() > filter.getMaxQtyLeft()) {
            throw new IllegalArgumentException("Số lượng tồn tối thiểu không được lớn hơn tối đa");
        }
    }

    private boolean hasFilter(InventoryLotFilter filter) {
        return filter.getProductId() != null
                || filter.getSupplierId() != null
                || (filter.getStatus() != null && !filter.getStatus().isBlank())
                || filter.getImportFrom() != null
                || filter.getImportTo() != null
                || filter.getExpiryFrom() != null
                || filter.getExpiryTo() != null
                || filter.getMinQtyLeft() != null
                || filter.getMaxQtyLeft() != null;
    }

    private void loadSelectedProductDetail(HttpServletRequest req, Long productId, LocalDate today) {
        Product selectedProduct = executor.execute(em -> productRepo.findById(em, productId).orElse(null));
        if (selectedProduct == null) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại.");
        }

        req.setAttribute("selectedProduct", selectedProduct);

        StockSummaryDto stockSummary = lotService.getStockSummary(productId);
        req.setAttribute("stockSummary", stockSummary);

        List<ProductLot> allLots = executor.execute(em -> em.createQuery(
                "SELECT l FROM ProductLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE p.id = :pid " +
                        "ORDER BY l.expiryDate ASC, l.importDate ASC, l.id ASC",
                ProductLot.class
        ).setParameter("pid", productId).getResultList());
        req.setAttribute("allLots", allLots);

        List<ProductLot> availableLots = allLots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> !l.getExpiryDate().isBefore(today))
                .collect(Collectors.toList());
        req.setAttribute("availableLots", availableLots);

        List<ProductLot> expiredLots = allLots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> l.getExpiryDate().isBefore(today))
                .collect(Collectors.toList());
        req.setAttribute("expiredLots", expiredLots);

        LocalDate sevenDaysLater = today.plusDays(7);
        List<ProductLot> upcomingExpiry = allLots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> !l.getExpiryDate().isBefore(today))
                .filter(l -> !l.getExpiryDate().isAfter(sevenDaysLater))
                .collect(Collectors.toList());
        req.setAttribute("upcomingExpiry", upcomingExpiry);
    }
}
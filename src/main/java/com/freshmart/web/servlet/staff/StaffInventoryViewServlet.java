package com.freshmart.web.servlet.staff;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.entity.Supplier;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.ProductLotRepository;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servlet để xem tồn kho theo lô và FEFO.
 * Map: /staff/inventory
 */
@WebServlet(urlPatterns = {"/staff/inventory"})
public class StaffInventoryViewServlet extends HttpServlet {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();
    private final ProductLotRepository lotRepo = new ProductLotRepository();
    private final SupplierRepository supplierRepo = new SupplierRepository();
    private final ProductLotService lotService = new ProductLotService();

    private Long parseLongOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return Long.parseLong(raw.trim());
    }

    private Integer parseIntOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return Integer.parseInt(raw.trim());
    }

    private LocalDate parseDateOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return LocalDate.parse(raw.trim());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            LocalDate today = LocalDate.now();
            req.setAttribute("today", today);

            List<Product> products = executor.execute(em -> productRepo.findAll(em, false));
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

            // Validation
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

            req.setAttribute("filter", filter);

            boolean hasFilter =
                    filter.getProductId() != null ||
                    filter.getSupplierId() != null ||
                    (filter.getStatus() != null && !filter.getStatus().isBlank()) ||
                    filter.getImportFrom() != null ||
                    filter.getImportTo() != null ||
                    filter.getExpiryFrom() != null ||
                    filter.getExpiryTo() != null ||
                    filter.getMinQtyLeft() != null ||
                    filter.getMaxQtyLeft() != null;

            if (hasFilter) {
                List<ProductLot> filteredLots = executor.execute(em -> lotRepo.searchLots(em, filter, today));
                long filteredCount = executor.execute(em -> lotRepo.countLots(em, filter, today));

                req.setAttribute("filteredLots", filteredLots);
                req.setAttribute("filteredCount", filteredCount);
            }

            // Load detailed data when product is selected (independent of hasFilter)
            if (filter.getProductId() != null) {
                Product selectedProduct = executor.execute(em ->
                        productRepo.findById(em, filter.getProductId()).orElse(null)
                );

                if (selectedProduct == null || !selectedProduct.isActive()) {
                    throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã ngừng kinh doanh.");
                }

                req.setAttribute("selectedProduct", selectedProduct);

                // Get comprehensive stock summary
                StockSummaryDto stockSummary = lotService.getStockSummary(filter.getProductId());
                req.setAttribute("stockSummary", stockSummary);

                // Get all lots (regardless of expiry)
                List<ProductLot> allLots = executor.execute(em ->
                    em.createQuery(
                            "SELECT l FROM ProductLot l WHERE l.product.id = :pid ORDER BY l.expiryDate ASC",
                            ProductLot.class
                    ).setParameter("pid", filter.getProductId())
                     .getResultList()
                );
                req.setAttribute("allLots", allLots);

                // Get available (non-expired) lots for FEFO display
                List<ProductLot> availableLots = allLots.stream()
                    .filter(l -> l.getExpiryDate().isAfter(today) || l.getExpiryDate().isEqual(today))
                    .filter(l -> l.getQtyLeft() > 0)
                    .collect(Collectors.toList());
                req.setAttribute("availableLots", availableLots);

                // Get expired lots (with remaining qty)
                List<ProductLot> expiredLots = allLots.stream()
                    .filter(l -> l.getExpiryDate().isBefore(today) && l.getQtyLeft() > 0)
                    .collect(Collectors.toList());
                req.setAttribute("expiredLots", expiredLots);

                // Get lots expiring within 7 days
                LocalDate sevenDaysLater = today.plusDays(7);
                List<ProductLot> upcomingExpiry = allLots.stream()
                    .filter(l -> (l.getExpiryDate().isAfter(today) || l.getExpiryDate().isEqual(today)) &&
                           (l.getExpiryDate().isBefore(sevenDaysLater) || l.getExpiryDate().isEqual(sevenDaysLater)))
                    .filter(l -> l.getQtyLeft() > 0)
                    .collect(Collectors.toList());
                req.setAttribute("upcomingExpiry", upcomingExpiry);
            }

        } catch (Exception ex) {
            req.setAttribute("errorMessage", ex.getMessage());
        }

        req.getRequestDispatcher("/WEB-INF/jsp/staff/inventory_view.jsp").forward(req, resp);
    }
}

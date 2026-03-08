package com.freshmart.web.servlet.staff;

import com.freshmart.entity.ProductLot;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.SupplierRepository;
import com.freshmart.service.ProductLotService;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Servlet để nhập lô sản phẩm (import lot / import stock).
 * Map: /staff/import-lot
 */
@WebServlet(urlPatterns = {"/staff/import-lot"})
public class StaffImportLotServlet extends HttpServlet {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();
    private final SupplierRepository supplierRepo = new SupplierRepository();
    private final ProductLotService lotService = new ProductLotService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Load products and suppliers for dropdown
        var products = executor.execute(em -> productRepo.findAll(em, false));
        var suppliers = executor.execute(supplierRepo::findAll);

        req.setAttribute("products", products);
        req.setAttribute("suppliers", suppliers);

        // Check if editing an existing lot
        String idRaw = req.getParameter("id");
        if (idRaw != null && !idRaw.isBlank()) {
            try {
                Long lotId = Long.parseLong(idRaw);
                ProductLot editingLot = lotService.getLotDetail(lotId)
                    .orElseThrow(() -> new IllegalArgumentException("Lot not found"));
                req.setAttribute("editingLot", editingLot);
            } catch (NumberFormatException ex) {
                req.setAttribute("errorMessage", "Invalid lot ID");
            }
        }

        req.getRequestDispatcher("/WEB-INF/jsp/staff/import_lot.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String lotIdRaw = req.getParameter("lotId");
            String productIdRaw = req.getParameter("productId");
            String supplierIdRaw = req.getParameter("supplierId");
            String importDateRaw = req.getParameter("importDate");
            String expiryDateRaw = req.getParameter("expiryDate");
            String qtyRaw = req.getParameter("quantity");
            String priceRaw = req.getParameter("importPrice");

            if (productIdRaw == null || productIdRaw.isBlank()) {
                throw new IllegalArgumentException("Product is required");
            }

            Long productId = Long.parseLong(productIdRaw);
            Long supplierId = (supplierIdRaw == null || supplierIdRaw.isBlank()) ? null : Long.parseLong(supplierIdRaw);
            int quantity = Integer.parseInt(qtyRaw != null && !qtyRaw.isBlank() ? qtyRaw : "0");

            DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
            LocalDate importDate = importDateRaw != null ? LocalDate.parse(importDateRaw, fmt) : LocalDate.now();
            LocalDate expiryDate = expiryDateRaw != null ? LocalDate.parse(expiryDateRaw, fmt) : null;

            BigDecimal importPrice = (priceRaw != null && !priceRaw.isBlank()) ? new BigDecimal(priceRaw) : BigDecimal.ZERO;

            // Perform import or update inside transaction
            executor.execute(em -> {
                if (lotIdRaw != null && !lotIdRaw.isBlank()) {
                    // Update existing lot
                    Long lotId = Long.parseLong(lotIdRaw);
                    ProductLot lot = lotService.updateLot(lotId, productId, supplierId, importDate, expiryDate, quantity, importPrice, em);
                    req.setAttribute("successMessage", "Cập nhật lô thành công! Lô ID: " + lot.getId());
                } else {
                    // Create new lot
                    ProductLot lot = lotService.importLot(productId, supplierId, importDate, expiryDate, quantity, importPrice, em);
                    req.setAttribute("successMessage", "Nhập lô thành công! Lô ID: " + lot.getId());
                }
                return null;
            });

            doGet(req, resp);
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            doGet(req, resp);
        }
    }
}

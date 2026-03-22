package com.freshmart.web.servlet.staff;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.ProductLot;
import com.freshmart.entity.User;
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

@WebServlet(urlPatterns = { "/staff/import-lot", "/staff/inventory/import" })
public class StaffImportLotServlet extends HttpServlet {

    private final JpaExecutor executor;
    private final ProductRepository productRepo;
    private final SupplierRepository supplierRepo;
    private final ProductLotService lotService;

    public StaffImportLotServlet() {
        this(new JpaExecutor(), new ProductRepository(), new SupplierRepository(), new ProductLotService());
    }

    StaffImportLotServlet(JpaExecutor executor,
            ProductRepository productRepo,
            SupplierRepository supplierRepo,
            ProductLotService lotService) {
        this.executor = executor;
        this.productRepo = productRepo;
        this.supplierRepo = supplierRepo;
        this.lotService = lotService;
    }

    private void loadReferenceData(HttpServletRequest req) {
        req.setAttribute("products", executor.execute(em -> productRepo.findAll(em, true)));
        req.setAttribute("suppliers", executor.execute(supplierRepo::findAll));
    }

    private void preserveSubmittedValues(HttpServletRequest req) {
        req.setAttribute("formProductId", req.getParameter("productId"));
        req.setAttribute("formSupplierId", req.getParameter("supplierId"));
        req.setAttribute("formImportDate", req.getParameter("importDate"));
        req.setAttribute("formExpiryDate", req.getParameter("expiryDate"));
        req.setAttribute("formQuantity", req.getParameter("quantity"));
        req.setAttribute("formImportPrice", req.getParameter("importPrice"));
    }

    private void loadEditingLotIfAny(HttpServletRequest req, String rawLotId) {
        if (rawLotId == null || rawLotId.isBlank()) {
            return;
        }

        try {
            Long lotId = Long.parseLong(rawLotId.trim());
            ProductLot editingLot = lotService.getLotDetail(lotId)
                    .orElseThrow(() -> new IllegalArgumentException("Lot not found"));
            req.setAttribute("editingLot", editingLot);
        } catch (NumberFormatException ex) {
            req.setAttribute("errorMessage", "Invalid lot ID");
        }
    }

    private String extractLotId(HttpServletRequest req) {
        String byId = req.getParameter("id");
        if (byId != null && !byId.isBlank()) {
            return byId;
        }
        return req.getParameter("lotId");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        loadReferenceData(req);
        loadEditingLotIfAny(req, extractLotId(req));

        if (req.getAttribute("editingLot") == null) {
            req.setAttribute("formProductId", req.getParameter("productId"));
            req.setAttribute("formSupplierId", req.getParameter("supplierId"));
        }

        req.getRequestDispatcher("/WEB-INF/jsp/staff/import_lot.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String lotIdRaw = req.getParameter("lotId");

        try {
            String productIdRaw = required(req.getParameter("productId"), "Product is required");
            String importDateRaw = required(req.getParameter("importDate"), "Import date is required");
            String expiryDateRaw = required(req.getParameter("expiryDate"), "Expiry date is required");
            String qtyRaw = required(req.getParameter("quantity"), "Quantity is required");

            String supplierIdRaw = trimToNull(req.getParameter("supplierId"));
            String priceRaw = trimToNull(req.getParameter("importPrice"));

            Long productId = Long.parseLong(productIdRaw);
            Long supplierId = supplierIdRaw == null ? null : Long.parseLong(supplierIdRaw);
            int quantity = Integer.parseInt(qtyRaw);

            DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
            LocalDate importDate = LocalDate.parse(importDateRaw, fmt);
            LocalDate expiryDate = LocalDate.parse(expiryDateRaw, fmt);
            BigDecimal importPrice = priceRaw == null ? BigDecimal.ZERO : new BigDecimal(priceRaw);

            User actor = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
            Long actorUserId = actor == null ? null : actor.getId();

            ProductLot savedLot = executor.execute(em -> {
                if (lotIdRaw != null && !lotIdRaw.isBlank()) {
                    Long lotId = Long.parseLong(lotIdRaw.trim());
                    return lotService.updateLot(
                            lotId,
                            productId,
                            supplierId,
                            importDate,
                            expiryDate,
                            quantity,
                            importPrice,
                            actorUserId,
                            em);
                }
                return lotService.importLot(
                        productId,
                        supplierId,
                        importDate,
                        expiryDate,
                        quantity,
                        importPrice,
                        actorUserId,
                        em);
            });

            if (lotIdRaw != null && !lotIdRaw.isBlank()) {
                req.setAttribute("successMessage", "Cập nhật lô thành công! Lô ID: " + savedLot.getId());
                req.setAttribute("editingLot", savedLot);
            } else {
                req.setAttribute("successMessage", "Nhập lô thành công! Lô ID: " + savedLot.getId());
            }
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            preserveSubmittedValues(req);
            loadEditingLotIfAny(req, lotIdRaw);
        }

        loadReferenceData(req);
        req.getRequestDispatcher("/WEB-INF/jsp/staff/import_lot.jsp").forward(req, resp);
    }

    private String required(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

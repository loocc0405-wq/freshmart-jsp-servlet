package com.freshmart.web.servlet.staff;

import com.freshmart.entity.Product;
import com.freshmart.entity.Supplier;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.SupplierRepository;
import com.freshmart.service.AppSettingService;
import com.freshmart.service.InventoryHistoryService;
import com.freshmart.service.InventoryReportService;
import com.freshmart.service.dto.InventoryLotFilter;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@WebServlet(urlPatterns = { "/staff/inventory-report", "/staff/inventory/report" })
public class StaffInventoryReportServlet extends HttpServlet {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();
    private final SupplierRepository supplierRepo = new SupplierRepository();
    private final InventoryReportService reportService = new InventoryReportService();
    private final InventoryHistoryService historyService = new InventoryHistoryService();
    private final AppSettingService appSettingService = new AppSettingService();

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

    private InventoryLotFilter buildFilter(HttpServletRequest req) {
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
        validateFilter(filter);
        return filter;
    }

    private void validateFilter(InventoryLotFilter filter) {
        if (filter.getImportFrom() != null
                && filter.getImportTo() != null
                && filter.getImportFrom().isAfter(filter.getImportTo())) {
            throw new IllegalArgumentException("Ngày nhập bắt đầu không được lớn hơn ngày nhập kết thúc");
        }

        if (filter.getExpiryFrom() != null
                && filter.getExpiryTo() != null
                && filter.getExpiryFrom().isAfter(filter.getExpiryTo())) {
            throw new IllegalArgumentException("HSD bắt đầu không được lớn hơn HSD kết thúc");
        }

        if (filter.getMinQtyLeft() != null
                && filter.getMaxQtyLeft() != null
                && filter.getMinQtyLeft() > filter.getMaxQtyLeft()) {
            throw new IllegalArgumentException("Tồn tối thiểu không được lớn hơn tồn tối đa");
        }
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

            int lowStockThreshold = appSettingService.getLowStockThreshold();
            int upcomingExpiryDays = appSettingService.getUpcomingExpiryDays();
            InventoryLotFilter filter = buildFilter(req);
            InventoryReportService.InventoryReportSnapshot snapshot = reportService.buildReportSnapshot(filter,
                    lowStockThreshold, upcomingExpiryDays);

            if ("csv".equalsIgnoreCase(req.getParameter("export"))) {
                writeCsv(resp, snapshot);
                return;
            }

            List<Product> products = executor.execute(em -> productRepo.findAll(em, true));
            List<Supplier> suppliers = executor.execute(supplierRepo::findAll);

            req.setAttribute("products", products);
            req.setAttribute("suppliers", suppliers);
            req.setAttribute("filter", filter);

            req.setAttribute("allProductsOverview", snapshot.getAllProductsOverview());
            req.setAttribute("lowStockProducts", snapshot.getLowStockProducts());
            req.setAttribute("upcomingExpiryProducts", snapshot.getUpcomingExpiryProducts());
            req.setAttribute("expiredLots", snapshot.getExpiredLots());

            req.setAttribute("totalInventoryValue", snapshot.getTotalInventoryValue());
            req.setAttribute("totalActiveLots", snapshot.getTotalActiveLots());
            req.setAttribute("nearExpiryValue", snapshot.getNearExpiryValue());
            req.setAttribute("expiredValue", snapshot.getExpiredValue());
            req.setAttribute("stagnantLotsCount", snapshot.getStagnantLotsCount());
            req.setAttribute("stagnantValue", snapshot.getStagnantValue());
            req.setAttribute("stagnantLotDays", InventoryReportService.DEFAULT_STAGNANT_DAYS);

            req.setAttribute("lowStockThreshold", lowStockThreshold);
            req.setAttribute("upcomingExpiryDays", upcomingExpiryDays);

            req.setAttribute("upcomingExpiryCount", snapshot.getUpcomingExpiryCount());
            req.setAttribute("expiredLotsCount", snapshot.getExpiredLotsCount());
            req.setAttribute("today", LocalDate.now());

            req.setAttribute("recentTransactions", historyService.getRecentTransactions(50));
            req.setAttribute("recentDisposals", historyService.getRecentDisposals(50));

        } catch (Exception ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            req.setAttribute("recentTransactions", Collections.emptyList());
            req.setAttribute("recentDisposals", Collections.emptyList());
        }

        req.getRequestDispatcher("/WEB-INF/jsp/staff/inventory_report.jsp").forward(req, resp);
    }

    private void writeCsv(HttpServletResponse resp,
            InventoryReportService.InventoryReportSnapshot snapshot) throws IOException {
        String fileName = "inventory-report-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        StringBuilder csv = new StringBuilder();
        csv.append('\ufeff');
        csv.append(
                "Product ID,Product Name,Total Qty In,Total Qty Left,Available Qty,Expired Qty,Consumed Qty,Active Lots,Expired Lots,Nearest Expiry,Available Value\n");

        for (InventoryReportService.ProductInventoryOverview row : snapshot.getAllProductsOverview()) {
            appendCsvCell(csv, row.getProductId());
            appendCsvCell(csv, row.getProductName());
            appendCsvCell(csv, row.getTotalQtyIn());
            appendCsvCell(csv, row.getTotalQtyLeft());
            appendCsvCell(csv, row.getAvailableQty());
            appendCsvCell(csv, row.getExpiredQty());
            appendCsvCell(csv, row.getTotalQtyConsumed());
            appendCsvCell(csv, row.getActiveLotsCount());
            appendCsvCell(csv, row.getExpiredLotsCount());
            appendCsvCell(csv, row.getNearestExpiry());
            appendCsvCell(csv, normalizeBigDecimal(row.getAvailableValue()), true);
        }

        resp.getWriter().write(csv.toString());
    }

    private String normalizeBigDecimal(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    private void appendCsvCell(StringBuilder csv, Object value) {
        appendCsvCell(csv, value, false);
    }

    private void appendCsvCell(StringBuilder csv, Object value, boolean last) {
        String raw = value == null ? "" : String.valueOf(value);
        String escaped = raw.replace("\"", "\"\"");
        csv.append('\"').append(escaped).append('\"');
        csv.append(last ? '\n' : ',');
    }
}

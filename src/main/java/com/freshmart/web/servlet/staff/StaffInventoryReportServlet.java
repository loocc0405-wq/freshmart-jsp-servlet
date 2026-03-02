package com.freshmart.web.servlet.staff;

import com.freshmart.service.InventoryReportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Servlet để xem báo cáo tồn kho.
 * Map: /staff/inventory-report
 */
@WebServlet(urlPatterns = {"/staff/inventory-report"})
public class StaffInventoryReportServlet extends HttpServlet {

    private final InventoryReportService reportService = new InventoryReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Get all product inventory overview
        var allProductsOverview = reportService.getAllProductInventoryOverview();
        
        // Get low stock products (< 50)
        var lowStockProducts = reportService.getLowStockProducts(50);
        
        // Get products with upcoming expiry (7 days)
        var upcomingExpiryProducts = reportService.getProductsWithUpcomingExpiry(7);
        
        // Get expired lots for cleanup
        var expiredLots = reportService.getExpiredLotsForCleanup();
        
        // Get summary metrics
        var totalInventoryValue = reportService.getTotalInventoryValue();
        var totalActiveLots = reportService.getTotalActiveLots();

        req.setAttribute("allProductsOverview", allProductsOverview);
        req.setAttribute("lowStockProducts", lowStockProducts);
        req.setAttribute("upcomingExpiryProducts", upcomingExpiryProducts);
        req.setAttribute("expiredLots", expiredLots);
        req.setAttribute("totalInventoryValue", totalInventoryValue);
        req.setAttribute("totalActiveLots", totalActiveLots);
        req.setAttribute("upcomingExpiryCount", upcomingExpiryProducts.size());
        req.setAttribute("expiredLotsCount", expiredLots.size());
        req.setAttribute("today", LocalDate.now());

        req.getRequestDispatcher("/WEB-INF/jsp/staff/inventory_report.jsp").forward(req, resp);
    }
}

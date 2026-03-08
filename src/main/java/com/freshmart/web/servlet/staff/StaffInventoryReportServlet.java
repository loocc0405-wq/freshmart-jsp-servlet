package com.freshmart.web.servlet.staff;

import com.freshmart.service.AppSettingService;
import com.freshmart.service.InventoryReportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet(urlPatterns = {"/staff/inventory-report"})
public class StaffInventoryReportServlet extends HttpServlet {

    private final InventoryReportService reportService = new InventoryReportService();
    private final AppSettingService appSettingService = new AppSettingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int lowStockThreshold = appSettingService.getLowStockThreshold();
        int upcomingExpiryDays = appSettingService.getUpcomingExpiryDays();

        var allProductsOverview = reportService.getAllProductInventoryOverview();
        var lowStockProducts = reportService.getLowStockProducts(lowStockThreshold);
        var upcomingExpiryProducts = reportService.getProductsWithUpcomingExpiry(upcomingExpiryDays);
        var expiredLots = reportService.getExpiredLotsForCleanup();

        var totalInventoryValue = reportService.getTotalInventoryValue();
        var totalActiveLots = reportService.getTotalActiveLots();

        req.setAttribute("allProductsOverview", allProductsOverview);
        req.setAttribute("lowStockProducts", lowStockProducts);
        req.setAttribute("upcomingExpiryProducts", upcomingExpiryProducts);
        req.setAttribute("expiredLots", expiredLots);
        req.setAttribute("totalInventoryValue", totalInventoryValue);
        req.setAttribute("totalActiveLots", totalActiveLots);

        req.setAttribute("lowStockThreshold", lowStockThreshold);
        req.setAttribute("upcomingExpiryDays", upcomingExpiryDays);

        req.setAttribute("upcomingExpiryCount", upcomingExpiryProducts.size());
        req.setAttribute("expiredLotsCount", expiredLots.size());
        req.setAttribute("today", LocalDate.now());

        req.getRequestDispatcher("/WEB-INF/jsp/staff/inventory_report.jsp").forward(req, resp);
    }
}
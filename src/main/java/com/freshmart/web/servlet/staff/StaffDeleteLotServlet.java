package com.freshmart.web.servlet.staff;

import com.freshmart.service.ProductLotService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet để xóa một lô hàng (thường dùng khi lô đã hết hạn hoặc bị loại bỏ).
 * Map: /staff/delete-lot?lotId=<id>&redirect=<path>
 */
@WebServlet(urlPatterns = { "/staff/delete-lot" })
public class StaffDeleteLotServlet extends HttpServlet {

    private final ProductLotService lotService;

    public StaffDeleteLotServlet() {
        this(new ProductLotService());
    }

    StaffDeleteLotServlet(ProductLotService lotService) {
        this.lotService = lotService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String lotIdRaw = req.getParameter("lotId");
        String redirect = req.getParameter("redirect");
        if (redirect == null || redirect.isBlank()) {
            redirect = "/staff/inventory";
        }

        // Sanitize redirect để chỉ cho redirect về 2 màn quản lý lot/report
        if (!(redirect.startsWith("/staff/inventory") || redirect.startsWith("/staff/inventory-report"))) {
            redirect = "/staff/inventory";
        }

        try {
            if (lotIdRaw != null) {
                Long lotId = Long.parseLong(lotIdRaw);
                lotService.deleteLot(lotId);
                req.getSession().setAttribute("successMessage", "Lô #" + lotId + " đã được loại bỏ.");
            } else {
                req.getSession().setAttribute("errorMessage", "Không có ID lô để xóa.");
            }
        } catch (NumberFormatException ex) {
            req.getSession().setAttribute("errorMessage", "ID lô không hợp lệ.");
        } catch (RuntimeException ex) {
            req.getSession().setAttribute("errorMessage", "Xảy ra lỗi khi xóa lô: " + ex.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + redirect);
    }
}
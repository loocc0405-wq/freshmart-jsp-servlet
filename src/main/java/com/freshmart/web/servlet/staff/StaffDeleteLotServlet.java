package com.freshmart.web.servlet.staff;

import com.freshmart.service.ProductLotService;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet để xóa một lô hàng (thường dùng khi lô đã hết hạn hoặc bị loại bỏ).
 * Map: /staff/delete-lot?lotId=<id>&redirect=<path>
 */
@WebServlet(urlPatterns = {"/staff/delete-lot"})
public class StaffDeleteLotServlet extends HttpServlet {

    private final ProductLotService lotService = new ProductLotService();
    private final JpaExecutor executor = new JpaExecutor();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String lotIdRaw = req.getParameter("lotId");
        String redirect = req.getParameter("redirect");
        if (redirect == null || redirect.isBlank()) {
            redirect = "/staff/inventory";
        }

        try {
            if (lotIdRaw != null) {
                Long lotId = Long.parseLong(lotIdRaw);
                executor.execute(em -> {
                    lotService.deleteLot(lotId);
                    return null;
                });
                req.getSession().setAttribute("successMessage", "Lô #" + lotId + " đã được xóa.");
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

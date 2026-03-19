package com.freshmart.web.servlet.staff;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.ProductLot;
import com.freshmart.entity.User;
import com.freshmart.service.ProductLotService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/staff/lot-disposals/new"})
public class StaffLotDisposalServlet extends HttpServlet {

    private final ProductLotService lotService = new ProductLotService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Long lotId = parseRequiredLong(req.getParameter("lotId"), "Thiếu lot ID.");
            ProductLot lot = lotService.getLotDetail(lotId)
                    .orElseThrow(() -> new IllegalArgumentException("Lot not found: " + lotId));
            req.setAttribute("lot", lot);
            req.setAttribute("redirect", sanitizeRedirect(req.getParameter("redirect")));
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
        }

        req.getRequestDispatcher("/WEB-INF/jsp/staff/lot_disposal.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String redirect = sanitizeRedirect(req.getParameter("redirect"));
        try {
            Long lotId = parseRequiredLong(req.getParameter("lotId"), "Thiếu lot ID.");
            int disposeQty = Integer.parseInt(req.getParameter("disposeQty"));
            String reason = req.getParameter("reason");
            String note = req.getParameter("note");
            User actor = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);

            lotService.disposeLot(lotId, disposeQty, reason, note, actor != null ? actor.getId() : null);
            req.getSession().setAttribute("successMessage", "Đã ghi nhận tiêu hủy cho lô #" + lotId + ".");
            resp.sendRedirect(req.getContextPath() + redirect);
            return;
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            try {
                Long lotId = parseRequiredLong(req.getParameter("lotId"), "Thiếu lot ID.");
                req.setAttribute("lot", lotService.getLotDetail(lotId).orElse(null));
            } catch (RuntimeException ignored) {
                // ignore nested lookup failure and show original error
            }
            req.setAttribute("redirect", redirect);
            req.getRequestDispatcher("/WEB-INF/jsp/staff/lot_disposal.jsp").forward(req, resp);
        }
    }

    private Long parseRequiredLong(String raw, String message) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return Long.parseLong(raw.trim());
    }

    private String sanitizeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return "/staff/inventory-report";
        }
        if (redirect.startsWith("/staff/inventory") || redirect.startsWith("/staff/inventory-report")) {
            return redirect;
        }
        return "/staff/inventory-report";
    }
}

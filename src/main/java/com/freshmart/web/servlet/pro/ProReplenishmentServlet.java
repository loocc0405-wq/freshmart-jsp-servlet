package com.freshmart.web.servlet.pro;

import com.freshmart.service.ReplenishmentService;
import com.freshmart.service.dto.ReplenishSuggestion;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/pro/replenishment"})
public class ProReplenishmentServlet extends HttpServlet {

    private final ReplenishmentService service = new ReplenishmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int daysHistory = parseInt(req.getParameter("history"), 30);
        int leadTimeDays = parseInt(req.getParameter("lead"), 3);
        int bufferDays = parseInt(req.getParameter("buffer"), 2);
        int safetyDays = parseInt(req.getParameter("safety"), 2);

        // chặn giá trị âm
        if (daysHistory <= 0) daysHistory = 30;
        if (leadTimeDays < 0) leadTimeDays = 0;
        if (bufferDays < 0) bufferDays = 0;
        if (safetyDays < 0) safetyDays = 0;

        List<ReplenishSuggestion> rows = service.suggest(daysHistory, leadTimeDays, bufferDays, safetyDays);

        req.setAttribute("rows", rows);
        req.setAttribute("daysHistory", daysHistory);
        req.setAttribute("leadTimeDays", leadTimeDays);
        req.setAttribute("bufferDays", bufferDays);
        req.setAttribute("safetyDays", safetyDays);

        req.getRequestDispatcher("/WEB-INF/jsp/pro/replenishment.jsp").forward(req, resp);
    }

    private int parseInt(String raw, int def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Integer.parseInt(raw);
        } catch (Exception e) {
            return def;
        }
    }
}
package com.freshmart.web.servlet.staff;

import com.freshmart.service.ReplenishmentService;
import com.freshmart.service.dto.ReplenishSuggestion;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/staff/forecast")
public class StaffForecastServlet extends HttpServlet {

    private final ReplenishmentService service = new ReplenishmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // có thể cho chỉnh bằng query param sau này
        int daysHistory = 30;
        int leadTimeDays = 3;
        int bufferDays = 0;   
        int safetyDays = 2;

        List<ReplenishSuggestion> rows = service.suggest(daysHistory, leadTimeDays, bufferDays, safetyDays);

        req.setAttribute("rows", rows);
        req.setAttribute("daysHistory", daysHistory);
        req.setAttribute("leadTimeDays", leadTimeDays);
        req.setAttribute("bufferDays", bufferDays);
        req.setAttribute("safetyDays", safetyDays);

        req.getRequestDispatcher("/WEB-INF/jsp/staff/forecast.jsp").forward(req, resp);
    }
}
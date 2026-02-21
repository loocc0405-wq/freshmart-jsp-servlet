package com.freshmart.web.servlet.pro;

import com.freshmart.service.ForecastService;
import com.freshmart.service.dto.ForecastPoint;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/pro/dashboard"})
public class ProDashboardServlet extends HttpServlet {

    private final ForecastService forecastService = new ForecastService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = defaultStr(req.getParameter("method"), "ma"); // ma | es
        int history = parseInt(req.getParameter("history"), 90);
        int horizon = parseInt(req.getParameter("horizon"), 30);

        List<ForecastPoint> points;
        if ("es".equalsIgnoreCase(method)) {
            double alpha = parseDouble(req.getParameter("alpha"), 0.3);
            points = forecastService.forecastExponentialSmoothing(history, horizon, alpha);
            req.setAttribute("alpha", alpha);
        } else {
            int window = parseInt(req.getParameter("window"), 7);
            points = forecastService.forecastMovingAverage(history, horizon, window);
            req.setAttribute("window", window);
        }

        // Build Chart.js arrays
        List<String> labels = new ArrayList<>();
        List<BigDecimal> actual = new ArrayList<>();
        List<BigDecimal> forecast = new ArrayList<>();

        for (ForecastPoint p : points) {
            labels.add(p.getDate().toString());
            actual.add(p.getActual());
            forecast.add(p.getForecast());
        }

        req.setAttribute("method", method);
        req.setAttribute("history", history);
        req.setAttribute("horizon", horizon);

        req.setAttribute("labelsJson", gson.toJson(labels));
        req.setAttribute("actualJson", gson.toJson(actual));
        req.setAttribute("forecastJson", gson.toJson(forecast));

        req.getRequestDispatcher("/WEB-INF/jsp/pro/dashboard.jsp").forward(req, resp);
    }

    private int parseInt(String raw, int def) {
        try { return raw == null ? def : Integer.parseInt(raw); }
        catch (Exception e) { return def; }
    }

    private double parseDouble(String raw, double def) {
        try { return raw == null ? def : Double.parseDouble(raw); }
        catch (Exception e) { return def; }
    }

    private String defaultStr(String raw, String def) {
        return (raw == null || raw.isBlank()) ? def : raw;
    }
}

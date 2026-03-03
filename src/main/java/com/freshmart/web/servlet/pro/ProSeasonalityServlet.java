package com.freshmart.web.servlet.pro;

import com.freshmart.service.SeasonalityService;
import com.freshmart.service.dto.SeasonalityPoint;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/pro/seasonality"})
public class ProSeasonalityServlet extends HttpServlet {

    private final SeasonalityService service = new SeasonalityService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int history = parseInt(req.getParameter("history"), 180);
        int window = parseInt(req.getParameter("window"), 7);
        double z = parseDouble(req.getParameter("z"), 1.5);

        List<SeasonalityPoint> points = service.analyze(history, window, z);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> actual = new ArrayList<>();
        List<BigDecimal> mean = new ArrayList<>();
        List<Double> zScore = new ArrayList<>();
        List<SeasonalityPoint> flagged = new ArrayList<>();

        for (SeasonalityPoint p : points) {
            labels.add(p.getDate().toString());
            actual.add(p.getActual());
            mean.add(p.getRollingMean());
            zScore.add(p.getZScore());
            if (p.getSignal() != null && !p.getSignal().isBlank()) flagged.add(p);
        }

        req.setAttribute("history", history);
        req.setAttribute("window", window);
        req.setAttribute("z", z);

        req.setAttribute("points", points);
        req.setAttribute("flagged", flagged);

        req.setAttribute("labelsJson", gson.toJson(labels));
        req.setAttribute("actualJson", gson.toJson(actual));
        req.setAttribute("meanJson", gson.toJson(mean));
        req.setAttribute("zJson", gson.toJson(zScore));

        req.getRequestDispatcher("/WEB-INF/jsp/pro/seasonality.jsp").forward(req, resp);
    }

    private int parseInt(String raw, int def) {
        try { return (raw == null || raw.isBlank()) ? def : Integer.parseInt(raw); }
        catch (Exception e) { return def; }
    }

    private double parseDouble(String raw, double def) {
        try { return (raw == null || raw.isBlank()) ? def : Double.parseDouble(raw); }
        catch (Exception e) { return def; }
    }
}
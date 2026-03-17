package com.freshmart.web.servlet.api;

import com.freshmart.service.ai.AiForecastEngineService;
import com.freshmart.util.AiConstants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

/**
 * REST API endpoint for hybrid AI-powered revenue forecasting.
 *
 * POST /api/ai/forecast
 * GET  /api/ai/forecast?period=month&productId=1
 */
@WebServlet("/api/ai/forecast")
public class AiForecastServlet extends HttpServlet {

    private static final Set<String> ALLOWED_PERIODS = Set.of("month", "quarter", "year");

    private AiForecastEngineService forecastEngine;

    @Override
    public void init() throws ServletException {
        super.init();
        forecastEngine = new AiForecastEngineService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleForecastRequest(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleForecastRequest(request, response, readRequestBody(request));
    }

    private void handleForecastRequest(HttpServletRequest request,
                                       HttpServletResponse response,
                                       String requestBody)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        ForecastRequest params;
        try {
            params = parseRequest(request, requestBody);
        } catch (IllegalArgumentException | JsonSyntaxException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        try {
            System.out.println("[AiForecastServlet] Generating hybrid forecast: period=" + params.period +
                    ", productId=" + params.productId);

            String forecastResult = forecastEngine.generateForecast(params.period, params.productId);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("forecast", forecastResult);
            jsonResponse.addProperty("period", params.period);
            if (params.productId != null) {
                jsonResponse.addProperty("productId", params.productId);
            }
            jsonResponse.addProperty("engine", "hybrid-baseline");
            jsonResponse.addProperty("aiConfigured", AiConstants.isGeminiConfigured());
            jsonResponse.addProperty("generatedAt", LocalDateTime.now().toString());

            response.getWriter().write(jsonResponse.toString());
        } catch (Exception ex) {
            log("Error in AiForecastServlet", ex);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Đã xảy ra lỗi khi tạo dự báo.");
        }
    }

    private ForecastRequest parseRequest(HttpServletRequest request, String requestBody) {
        String period = normalizePeriod(request.getParameter("period"));
        Long productId = parseProductId(request.getParameter("productId"));

        if (requestBody != null && !requestBody.isBlank()) {
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            if (jsonRequest.has("period") && !jsonRequest.get("period").isJsonNull()) {
                period = normalizePeriod(jsonRequest.get("period").getAsString());
            }
            if (jsonRequest.has("productId") && !jsonRequest.get("productId").isJsonNull()) {
                productId = jsonRequest.get("productId").getAsLong();
                if (productId != null && productId <= 0) {
                    throw new IllegalArgumentException("productId phải là số dương.");
                }
            }
        }

        ForecastRequest out = new ForecastRequest();
        out.period = period;
        out.productId = productId;
        return out;
    }

    private String normalizePeriod(String value) {
        if (value == null || value.isBlank()) {
            return "month";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_PERIODS.contains(normalized)) {
            throw new IllegalArgumentException("period chỉ chấp nhận: month, quarter, year.");
        }
        return normalized;
    }

    private Long parseProductId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException("productId phải là số dương.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("productId không hợp lệ.");
        }
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString().trim();
    }

    private void writeError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);

        JsonObject error = new JsonObject();
        error.addProperty("status", "error");
        error.addProperty("message", message);

        response.getWriter().write(error.toString());
    }

    private static final class ForecastRequest {
        private String period;
        private Long productId;
    }
}

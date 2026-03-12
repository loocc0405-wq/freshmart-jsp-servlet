package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.chat.ChatbotService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/api/chatbot/message")
public class ChatbotServlet extends HttpServlet {

    private ChatbotService chatbotService;

    @Override
    public void init() throws ServletException {
        super.init();
        chatbotService = new ChatbotService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        String requestBody = readRequestBody(request);
        if (requestBody.isBlank()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống.");
            return;
        }

        try {
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();

            String userMessage = getString(jsonRequest, "message");
            String sessionToken = getString(jsonRequest, "sessionToken");

            if (sessionToken == null || sessionToken.isBlank()) {
                sessionToken = UUID.randomUUID().toString();
            }

            if (userMessage == null || userMessage.isBlank()) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Tin nhắn không được để trống.");
                return;
            }

            Long userId = extractLoggedInUserId(request);

            String aiResponse;
            try {
                aiResponse = chatbotService.processUserMessage(sessionToken, userId, userMessage);
            } catch (Exception serviceEx) {
                log("ChatbotService failed", serviceEx);
                aiResponse = "Xin lỗi, hiện tại chatbot đang gặp sự cố tạm thời. Bạn vui lòng thử lại sau.";
            }

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("reply", aiResponse);
            jsonResponse.addProperty("sessionToken", sessionToken);

            response.getWriter().write(jsonResponse.toString());

        } catch (JsonSyntaxException | IllegalStateException ex) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu JSON không hợp lệ.");
        } catch (Exception ex) {
            log("Unexpected error in ChatbotServlet", ex);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Đã xảy ra lỗi khi xử lý chatbot.");
        }
    }

    private Long extractLoggedInUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object authUser = session.getAttribute(AppConstants.SESSION_USER);
        if (authUser instanceof User) {
            return ((User) authUser).getId();
        }

        Object legacyUser = session.getAttribute("user");
        if (legacyUser instanceof User) {
            return ((User) legacyUser).getId();
        }

        return null;
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

    private String getString(JsonObject jsonObject, String key) {
        if (jsonObject == null || !jsonObject.has(key) || jsonObject.get(key).isJsonNull()) {
            return null;
        }
        return jsonObject.get(key).getAsString().trim();
    }

    private void writeError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);

        JsonObject error = new JsonObject();
        error.addProperty("status", "error");
        error.addProperty("message", message);

        response.getWriter().write(error.toString());
    }
}
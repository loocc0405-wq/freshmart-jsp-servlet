package com.freshmart.web.servlet;

import com.freshmart.entity.User;
import com.freshmart.service.chat.ChatbotService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        try {
            JsonObject jsonRequest = JsonParser.parseString(sb.toString()).getAsJsonObject();

            String userMessage = jsonRequest.has("message") && !jsonRequest.get("message").isJsonNull()
                    ? jsonRequest.get("message").getAsString().trim()
                    : "";

            String sessionToken = jsonRequest.has("sessionToken") && !jsonRequest.get("sessionToken").isJsonNull()
                    ? jsonRequest.get("sessionToken").getAsString()
                    : null;

            if (sessionToken == null || sessionToken.isBlank()) {
                sessionToken = UUID.randomUUID().toString();
            }

            if (userMessage.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject error = new JsonObject();
                error.addProperty("status", "error");
                error.addProperty("message", "Tin nhắn không được để trống.");
                response.getWriter().write(error.toString());
                return;
            }

            Long userId = null;
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("user") instanceof User) {
                User user = (User) session.getAttribute("user");
                userId = user.getId();
            }

            String aiResponse = chatbotService.processUserMessage(sessionToken, userId, userMessage);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("reply", aiResponse);
            jsonResponse.addProperty("sessionToken", sessionToken);

            response.getWriter().write(jsonResponse.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("message", "Internal Server Error during AI Processing");

            response.getWriter().write(error.toString());
        }
    }
}
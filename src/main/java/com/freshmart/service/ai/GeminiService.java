package com.freshmart.service.ai;

import com.freshmart.util.AiConstants;
import com.freshmart.util.HttpClientUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;

public class GeminiService {

    public boolean isConfigured() {
        return AiConstants.isGeminiEnabled();
    }

    public String generateResponse(String systemPrompt, String userMessage, List<String[]> history) {
        try {
            String apiKey = safe(AiConstants.resolveGeminiApiKey());
            String apiUrl = safe(AiConstants.GEMINI_API_URL);
            String prompt = safe(systemPrompt);
            String message = safe(userMessage);

            if (apiKey.isBlank()) {
                System.out.println("[GeminiService] Gemini disabled because no API key was configured.");
                return null;
            }
            if (apiUrl.isBlank() || message.isBlank()) {
                System.err.println("[GeminiService] Missing API URL or message.");
                return null;
            }

            JsonObject payload = buildPayload(prompt, message, history);
            System.out.println("[GeminiService] Sending request to Gemini API...");
            String responseJson = HttpClientUtil.postJson(apiUrl, payload.toString(), apiKey);

            String parsed = parseGeminiResponse(responseJson);
            if (parsed.isBlank()) {
                System.err.println("[GeminiService] Gemini returned empty response.");
                return null;
            }

            System.out.println("[GeminiService] Got response (" + parsed.length() + " chars)");
            return parsed;

        } catch (Exception e) {
            System.err.println("[GeminiService] Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return null;
        }
    }

    public String generateResponse(String systemPrompt, String userMessage) {
        return generateResponse(systemPrompt, userMessage, null);
    }

    private JsonObject buildPayload(String systemPrompt, String userMessage, List<String[]> history) {
        JsonObject root = new JsonObject();

        if (!systemPrompt.isBlank()) {
            JsonObject systemInstruction = new JsonObject();
            JsonArray systemParts = new JsonArray();

            JsonObject systemText = new JsonObject();
            systemText.addProperty("text", systemPrompt);
            systemParts.add(systemText);

            systemInstruction.add("parts", systemParts);
            root.add("system_instruction", systemInstruction);
        }

        JsonArray contents = new JsonArray();
        if (history != null && !history.isEmpty()) {
            for (String[] turn : history) {
                if (turn == null || turn.length < 2 || turn[0] == null || turn[1] == null) {
                    continue;
                }
                String role = "user".equalsIgnoreCase(turn[0]) ? "user" : "model";
                JsonObject turnContent = new JsonObject();
                turnContent.addProperty("role", role);

                JsonArray turnParts = new JsonArray();
                JsonObject turnText = new JsonObject();
                turnText.addProperty("text", turn[1]);
                turnParts.add(turnText);

                turnContent.add("parts", turnParts);
                contents.add(turnContent);
            }
        }

        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray userParts = new JsonArray();
        JsonObject userText = new JsonObject();
        userText.addProperty("text", userMessage);
        userParts.add(userText);
        userContent.add("parts", userParts);
        contents.add(userContent);

        root.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.35);
        generationConfig.addProperty("maxOutputTokens", 1536);
        generationConfig.addProperty("topP", 0.9);
        root.add("generationConfig", generationConfig);

        return root;
    }

    private String parseGeminiResponse(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return "";
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            if (jsonObject.has("error") && jsonObject.get("error").isJsonObject()) {
                JsonObject error = jsonObject.getAsJsonObject("error");
                String errorMsg = error.has("message") ? error.get("message").getAsString() : "Unknown error";
                System.err.println("[GeminiService] API Error: " + errorMsg);
                return "";
            }

            JsonArray candidates = jsonObject.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                System.err.println("[GeminiService] No candidates in response.");
                return "";
            }

            JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            if (!firstCandidate.has("content") || firstCandidate.get("content").isJsonNull()) {
                return "";
            }

            JsonObject content = firstCandidate.getAsJsonObject("content");
            JsonArray parts = content.getAsJsonArray("parts");
            if (parts == null || parts.isEmpty()) {
                return "";
            }

            StringBuilder answer = new StringBuilder();
            for (int i = 0; i < parts.size(); i++) {
                JsonObject part = parts.get(i).getAsJsonObject();
                if (part.has("text") && !part.get("text").isJsonNull()) {
                    if (answer.length() > 0) {
                        answer.append("\n");
                    }
                    answer.append(part.get("text").getAsString());
                }
            }

            return answer.toString().trim();

        } catch (Exception e) {
            System.err.println("[GeminiService] Parse error: " + e.getMessage());
            return "";
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

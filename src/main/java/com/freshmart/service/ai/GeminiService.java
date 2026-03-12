package com.freshmart.service.ai;

import com.freshmart.util.AiConstants;
import com.freshmart.util.HttpClientUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GeminiService {

    public String generateResponse(String systemPrompt, String userMessage) {
        try {
            String apiKey = safe(AiConstants.GEMINI_API_KEY);
            String apiUrl = safe(AiConstants.GEMINI_API_URL);
            String prompt = safe(systemPrompt);
            String message = safe(userMessage);

            if (apiKey.isBlank() || apiUrl.isBlank() || message.isBlank()) {
                return AiConstants.FALLBACK_RESPONSE;
            }

            JsonObject payload = buildPayload(prompt, message);
            String responseJson = HttpClientUtil.postJson(apiUrl, payload.toString(), apiKey);

            String parsed = parseGeminiResponse(responseJson);
            return parsed.isBlank() ? AiConstants.FALLBACK_RESPONSE : parsed;

        } catch (Exception e) {
            return AiConstants.FALLBACK_RESPONSE;
        }
    }

    private JsonObject buildPayload(String systemPrompt, String userMessage) {
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
        generationConfig.addProperty("temperature", 0.4);
        generationConfig.addProperty("maxOutputTokens", 512);
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
                if (error.has("message") && !error.get("message").isJsonNull()) {
                    return "";
                }
            }

            JsonArray candidates = jsonObject.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
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
            return "";
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
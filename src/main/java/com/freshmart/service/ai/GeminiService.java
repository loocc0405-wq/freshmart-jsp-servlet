package com.freshmart.service.ai;

import com.freshmart.util.AiConstants;
import com.freshmart.util.HttpClientUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GeminiService {

    public String generateResponse(String systemPrompt, String userMessage) {
        try {
            JsonObject root = new JsonObject();
            JsonArray contents = new JsonArray();
            
            // System instructions
            JsonObject systemObj = new JsonObject();
            systemObj.addProperty("role", "user"); // Role must be 'user' or 'model' usually, we'll prefix system instruction.
            JsonArray systemParts = new JsonArray();
            JsonObject systemContent = new JsonObject();
            systemContent.addProperty("text", "[HƯỚNG DẪN HỆ THỐNG]:\n" + systemPrompt);
            systemParts.add(systemContent);
            systemObj.add("parts", systemParts);
            contents.add(systemObj);
            
            // User message
            JsonObject userObj = new JsonObject();
            userObj.addProperty("role", "user");
            JsonArray userParts = new JsonArray();
            JsonObject userContent = new JsonObject();
            userContent.addProperty("text", userMessage);
            userParts.add(userContent);
            userObj.add("parts", userParts);
            contents.add(userObj);
            
            root.add("contents", contents);

            String jsonPayload = root.toString();
            String responseJson = HttpClientUtil.postJson(AiConstants.GEMINI_API_URL, jsonPayload, AiConstants.GEMINI_API_KEY);
            return parseGeminiResponse(responseJson);

        } catch (Exception e) {
            e.printStackTrace();
            return AiConstants.FALLBACK_RESPONSE;
        }
    }

    private String parseGeminiResponse(String jsonString) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonArray candidates = jsonObject.getAsJsonArray("candidates");
            if (candidates != null && candidates.size() > 0) {
                JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                JsonObject content = firstCandidate.getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");
                if (parts != null && parts.size() > 0) {
                    return parts.get(0).getAsJsonObject().get("text").getAsString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AiConstants.FALLBACK_RESPONSE;
    }
}

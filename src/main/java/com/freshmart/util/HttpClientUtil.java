package com.freshmart.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClientUtil {

    public static String postJson(String targetUrl, String jsonPayload, String apiKey) throws Exception {
        // Append API key as query parameter (required by Gemini REST API)
        String fullUrl = targetUrl;
        if (apiKey != null && !apiKey.isEmpty()) {
            fullUrl += (targetUrl.contains("?") ? "&" : "?") + "key=" + apiKey;
        }

        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        BufferedReader br;
        
        if (responseCode >= 200 && responseCode < 300) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
        }

        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        
        if (responseCode >= 400) {
            System.err.println("API Error Response: " + response.toString());
            throw new Exception("API request failed with HTTP " + responseCode);
        }

        return response.toString();
    }
}

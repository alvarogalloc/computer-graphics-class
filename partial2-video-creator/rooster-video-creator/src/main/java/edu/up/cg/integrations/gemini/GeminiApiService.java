package edu.up.cg.integrations.gemini;

import edu.up.cg.health.ServiceHealth;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiApiService implements GeminiService {
    private final String apiKey;
    private final HttpClient httpClient;

    public GeminiApiService() {
        this(System.getenv("GEMINI_API_KEY"), HttpClient.newHttpClient());
    }

    public GeminiApiService(String apiKey, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    @Override
    public ServiceHealth healthCheck() {
        if (apiKey == null || apiKey.isBlank()) {
            return ServiceHealth.unhealthy("Gemini", "GEMINI_API_KEY is missing");
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + apiKey;
        String body = "{\"contents\":[{\"parts\":[{\"text\":\"Respond with OK\"}]}]}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body().contains("candidates")) {
                return ServiceHealth.healthy("Gemini", "Gemini API responded correctly");
            }
            return ServiceHealth.unhealthy("Gemini", "HTTP " + response.statusCode() + ": " + summarize(response.body()));
        } catch (IOException e) {
            return ServiceHealth.unhealthy("Gemini", "I/O error while calling Gemini API: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ServiceHealth.unhealthy("Gemini", "Health check interrupted");
        }
    }

    private String summarize(String text) {
        if (text == null) {
            return "No body";
        }
        String trimmed = text.replace('\n', ' ').replace('\r', ' ').trim();
        return trimmed.length() <= 200 ? trimmed : trimmed.substring(0, 200) + "...";
    }
}

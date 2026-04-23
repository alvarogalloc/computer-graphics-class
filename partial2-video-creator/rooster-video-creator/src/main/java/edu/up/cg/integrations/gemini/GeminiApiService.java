package edu.up.cg.integrations.gemini;

import edu.up.cg.integrations.ai.AIService;
import edu.up.cg.integrations.ai.AiTask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiApiService extends AIService {
    // pro gemini, others where not good.
    private static final String MODEL_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=";
    private static final Pattern TEXT_PATTERN = Pattern.compile("\\\"text\\\"\\s*:\\s*\\\"(.*?)\\\"", Pattern.DOTALL);

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
    public String generateEssenceImagePrompt(String mediaSummary) {
        return generateFromTask(AiTask.ESSENCE_IMAGE_PROMPT, mediaSummary);
    }

    @Override
    public String generateNarrationScript(String timelineSummary) {
        return generateFromTask(AiTask.NARRATION_SCRIPT, timelineSummary);
    }

    @Override
    public String generateInspirationalPhrase(String placesSummary) {
        return generateFromTask(AiTask.INSPIRATIONAL_PHRASE, placesSummary);
    }

    private String generateFromTask(AiTask task, String context) {
        return callGemini(task.promptPrefix() + context);
    }

    private String callGemini(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is missing");
        }

        String endpoint = MODEL_ENDPOINT + apiKey;
        String body = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapeJson(prompt) + "\"}]}]}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Gemini HTTP " + response.statusCode() + ": " + summarize(response.body()));
            }

            String text = extractText(response.body());
            if (text.isBlank()) {
                throw new IllegalStateException("Gemini response did not include candidate text");
            }
            return text;
        } catch (IOException e) {
            throw new IllegalStateException("I/O error while calling Gemini API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gemini request interrupted", e);
        }
    }

    private String extractText(String body) {
        Matcher matcher = TEXT_PATTERN.matcher(body);
        if (!matcher.find()) {
            return "";
        }
        return unescapeJson(matcher.group(1)).trim();
    }

    private String escapeJson(String text) {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

    private String unescapeJson(String text) {
        return text
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\");
    }

    private String summarize(String text) {
        if (text == null) {
            return "No body";
        }
        String trimmed = text.replace('\n', ' ').replace('\r', ' ').trim();
        return trimmed.length() <= 200 ? trimmed : trimmed.substring(0, 200) + "...";
    }

    @Override
    public void generateImage(String prompt, java.nio.file.Path outputFile) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is missing");
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/imagen-4.0-fast-generate-001:predict?key=" + apiKey;
        String body = "{\"instances\":[{\"prompt\":\"" + escapeJson(prompt) + "\"}],\"parameters\":{\"sampleCount\":1}}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Gemini Image HTTP " + response.statusCode() + ": " + summarize(response.body()));
            }

            String b64 = extractImageBase64(response.body());
            if (b64.isBlank()) {
                throw new IllegalStateException("Gemini response did not include image base64");
            }
            
            java.nio.file.Files.write(outputFile, java.util.Base64.getDecoder().decode(b64));
        } catch (IOException e) {
            throw new IllegalStateException("I/O error while calling Gemini Image API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gemini Image request interrupted", e);
        }
    }

    private static final java.util.regex.Pattern IMAGE_BASE64_PATTERN = java.util.regex.Pattern.compile("\"bytesBase64Encoded\"\\s*:\\s*\"(.*?)\"", java.util.regex.Pattern.DOTALL);

    private String extractImageBase64(String body) {
        java.util.regex.Matcher matcher = IMAGE_BASE64_PATTERN.matcher(body);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1).trim();
    }
}

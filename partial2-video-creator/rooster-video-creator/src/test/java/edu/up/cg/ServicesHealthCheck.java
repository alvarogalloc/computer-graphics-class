package edu.up.cg;

import edu.up.cg.integrations.ai.AIService;
import edu.up.cg.integrations.exiftool.ExifToolCliService;
import edu.up.cg.integrations.ffmpeg.FFmpegService;
import edu.up.cg.integrations.ffmpeg.FFmpegCliService;
import edu.up.cg.integrations.gemini.GeminiApiService;
import edu.up.cg.integrations.metadata.MetadataService;
import edu.up.cg.integrations.map.MapService;
import edu.up.cg.integrations.map.MapboxOsmService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServicesHealthCheck {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Test
    void allConfiguredServicesShouldBeHealthy() {
        MetadataService metadataService = new ExifToolCliService();
        FFmpegService ffmpegService = new FFmpegCliService();
        AIService aiService = new GeminiApiService();
        MapService mapService = new MapboxOsmService();

        assertTrue(metadataService != null, "MetadataService instance should exist");
        assertTrue(ffmpegService != null, "FFmpegService instance should exist");
        assertTrue(aiService != null, "AIService instance should exist");
        assertTrue(mapService != null, "MapService instance should exist");

        assertCliAvailable("ExifTool", "exiftool", "-ver");
        assertCliAvailable("FFmpeg", "ffmpeg", "-version");
        assertGeminiApiReachable();
        assertMapboxApiReachable();
    }

    private void assertCliAvailable(String serviceName, String command, String arg) {
        ProcessBuilder processBuilder = new ProcessBuilder(command, arg);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            int code = process.waitFor();
            report(serviceName, code == 0, "exit code " + code);
            assertTrue(code == 0, serviceName + " command failed with exit code " + code);
        } catch (IOException e) {
            report(serviceName, false, "not found: " + e.getMessage());
            assertTrue(false, serviceName + " command not found or not executable: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            report(serviceName, false, "interrupted");
            assertTrue(false, serviceName + " check interrupted");
        }
    }

    private void assertGeminiApiReachable() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        assertTrue(apiKey != null && !apiKey.isBlank(), "GEMINI_API_KEY is missing");

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent?key=" + apiKey;
        String body = "{\"contents\":[{\"parts\":[{\"text\":\"Respond exactly with: OK\"}]}]}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            boolean ok = response.statusCode() == 200 && response.body().contains("candidates");
            report("Gemini", ok, "HTTP " + response.statusCode());
            assertTrue(ok, "Gemini API check failed: HTTP " + response.statusCode() + " body: " + summarize(response.body()));
        } catch (IOException e) {
            report("Gemini", false, "I/O error");
            assertTrue(false, "Gemini API I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            report("Gemini", false, "interrupted");
            assertTrue(false, "Gemini API check interrupted");
        }
    }

    private void assertMapboxApiReachable() {
        String token = System.getenv("MAPBOX_ACCESS_TOKEN");
        assertTrue(token != null && !token.isBlank(), "MAPBOX_ACCESS_TOKEN is missing");

        String endpoint = "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/0,0,1/100x100?access_token=" + token;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .GET()
            .build();

        try {
            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
            boolean ok = response.statusCode() == 200 && response.body().length > 0;
            report("Mapbox OSM", ok, "HTTP " + response.statusCode());
            assertTrue(ok, "Mapbox API check failed: HTTP " + response.statusCode());
        } catch (IOException e) {
            report("Mapbox OSM", false, "I/O error");
            assertTrue(false, "Mapbox API I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            report("Mapbox OSM", false, "interrupted");
            assertTrue(false, "Mapbox API check interrupted");
        }
    }

    private String summarize(String body) {
        if (body == null) {
            return "No body";
        }
        String normalized = body.replace('\n', ' ').replace('\r', ' ').trim();
        return normalized.length() <= 200 ? normalized : normalized.substring(0, 200) + "...";
    }

    private void report(String serviceName, boolean success, String details) {
        System.out.println(serviceName + " -> " + (success ? "OK" : "FAIL") + " | " + details);
    }
}

package edu.up.cg.integrations.map;

import edu.up.cg.integrations.metadata.GeoPoint;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MapboxOsmService implements MapService {
    private final String accessToken;
    private final HttpClient httpClient;
    private final MapStyle style;

    public MapboxOsmService() {
        this(System.getenv("MAPBOX_ACCESS_TOKEN"), HttpClient.newHttpClient(), MapStyle.STREETS);
    }

    public MapboxOsmService(String accessToken, HttpClient httpClient) {
        this(accessToken, httpClient, MapStyle.STREETS);
    }

    public MapboxOsmService(String accessToken, HttpClient httpClient, MapStyle style) {
        this.accessToken = accessToken;
        this.httpClient = httpClient;
        this.style = style;
    }

    @Override
    public URI buildStaticMapUrl(GeoPoint firstLocation, GeoPoint lastLocation, int width, int height) {
        requireAccessToken();
        validateSize(width, height);
        String firstMarker = "pin-s-a+00a86b(%s,%s)".formatted(firstLocation.getLongitude(), firstLocation.getLatitude());
        String lastMarker = "pin-s-b+e63946(%s,%s)".formatted(lastLocation.getLongitude(), lastLocation.getLatitude());

        return URI.create(
            "https://api.mapbox.com/styles/v1/%s/static/%s,%s/auto/%sx%s?padding=60&access_token=%s"
                .formatted(style.value(), firstMarker, lastMarker, width, height, encode(accessToken))
        );
    }

    @Override
    public void downloadStaticMap(GeoPoint firstLocation, GeoPoint lastLocation, int width, int height, Path outputFile) {
        URI mapUri = buildStaticMapUrl(firstLocation, lastLocation, width, height);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(mapUri)
            .GET()
            .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200 || response.body().length == 0) {
                throw new IllegalStateException("Mapbox request failed with HTTP " + response.statusCode());
            }
            Files.write(outputFile, response.body());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to download static map: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Mapbox request interrupted", e);
        }
    }

    private void validateSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
    }

    private void requireAccessToken() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("MAPBOX_ACCESS_TOKEN is missing");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

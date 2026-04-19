package edu.up.cg.integrations.map;

import edu.up.cg.health.ServiceHealth;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MapboxOsmService implements MapService {
    private final String accessToken;
    private final HttpClient httpClient;

    public MapboxOsmService() {
        this(System.getenv("MAPBOX_ACCESS_TOKEN"), HttpClient.newHttpClient());
    }

    public MapboxOsmService(String accessToken, HttpClient httpClient) {
        this.accessToken = accessToken;
        this.httpClient = httpClient;
    }

    @Override
    public ServiceHealth healthCheck() {
        if (accessToken == null || accessToken.isBlank()) {
            return ServiceHealth.unhealthy("Mapbox OSM", "MAPBOX_ACCESS_TOKEN is missing");
        }

        String endpoint = "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/0,0,1/100x100?access_token=" + accessToken;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .GET()
            .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200 && response.body().length > 0) {
                return ServiceHealth.healthy("Mapbox OSM", "Mapbox static map endpoint is reachable");
            }
            return ServiceHealth.unhealthy("Mapbox OSM", "HTTP " + response.statusCode());
        } catch (IOException e) {
            return ServiceHealth.unhealthy("Mapbox OSM", "I/O error while calling Mapbox API: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ServiceHealth.unhealthy("Mapbox OSM", "Health check interrupted");
        }
    }
}

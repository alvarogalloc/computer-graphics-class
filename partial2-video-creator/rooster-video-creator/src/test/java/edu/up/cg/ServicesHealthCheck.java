package edu.up.cg;

import edu.up.cg.health.ServiceHealth;
import edu.up.cg.integrations.exiftool.ExifToolCliService;
import edu.up.cg.integrations.exiftool.ExifToolService;
import edu.up.cg.integrations.ffmpeg.FFmpegCliService;
import edu.up.cg.integrations.ffmpeg.FFmpegService;
import edu.up.cg.integrations.gemini.GeminiApiService;
import edu.up.cg.integrations.gemini.GeminiService;
import edu.up.cg.integrations.map.MapService;
import edu.up.cg.integrations.map.MapboxOsmService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServicesHealthCheck {

    @Test
    void allConfiguredServicesShouldBeHealthy() {
        ExifToolService exifToolService = new ExifToolCliService();
        FFmpegService ffmpegService = new FFmpegCliService();
        GeminiService geminiService = new GeminiApiService();
        MapService mapService = new MapboxOsmService();

        assertHealthy(exifToolService.healthCheck());
        assertHealthy(ffmpegService.healthCheck());
        assertHealthy(geminiService.healthCheck());
        assertHealthy(mapService.healthCheck());
    }

    private void assertHealthy(ServiceHealth health) {
        assertTrue(
            health.isHealthy(),
            health.getServiceName() + " is not healthy: " + health.getDetails()
        );
    }
}

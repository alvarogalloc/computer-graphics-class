package edu.up.cg.integrations.map;

import edu.up.cg.integrations.metadata.GeoPoint;

import java.net.URI;
import java.nio.file.Path;

public interface MapService {
    URI buildStaticMapUrl(GeoPoint firstLocation, GeoPoint lastLocation, int width, int height);

    void downloadStaticMap(GeoPoint firstLocation, GeoPoint lastLocation, int width, int height, Path outputFile);
}

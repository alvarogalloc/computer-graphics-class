package edu.up.cg.integrations.metadata;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

public final class MediaMetadata {
    private final Path sourcePath;
    private final MediaType mediaType;
    private final Optional<LocalDateTime> capturedAt;
    private final Optional<GeoPoint> location;

    public MediaMetadata(Path sourcePath, MediaType mediaType, Optional<LocalDateTime> capturedAt, Optional<GeoPoint> location) {
        this.sourcePath = sourcePath;
        this.mediaType = mediaType;
        this.capturedAt = capturedAt;
        this.location = location;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public Optional<LocalDateTime> getCapturedAt() {
        return capturedAt;
    }

    public Optional<GeoPoint> getLocation() {
        return location;
    }
}

package edu.up.cg.integrations.metadata;

import java.nio.file.Path;

public interface MetadataService {
    MediaMetadata readMetadata(Path mediaPath);
}

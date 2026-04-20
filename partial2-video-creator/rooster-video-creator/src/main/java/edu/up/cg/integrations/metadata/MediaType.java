package edu.up.cg.integrations.metadata;

public enum MediaType {
    IMAGE,
    VIDEO,
    UNKNOWN;

    public static MediaType fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return UNKNOWN;
        }
        if (mimeType.startsWith("image/")) {
            return IMAGE;
        }
        if (mimeType.startsWith("video/")) {
            return VIDEO;
        }
        return UNKNOWN;
    }
}

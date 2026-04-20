package edu.up.cg.integrations.ffmpeg;

public enum PortraitResolution {
    HD_1080x1920(1080, 1920);

    private final int width;
    private final int height;

    PortraitResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}

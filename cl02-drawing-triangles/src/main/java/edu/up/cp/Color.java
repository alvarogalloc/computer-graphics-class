package edu.up.cp;

/**
 * Color class for RGB color representation.
 * Provides basic color operations and predefined color constants.
 * Colors are stored as RGB values with 0-255 range for each component.
 */
public class Color {
    private final int red;
    private final int green;
    private final int blue;

    public Color(int red, int green, int blue) {
        this.red = Math.max(0, Math.min(255, red));
        this.green = Math.max(0, Math.min(255, green));
        this.blue = Math.max(0, Math.min(255, blue));
    }

    public Color(int rgb) {
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = rgb & 0xFF;
    }

    public int getRed() { return red; }
    public int getGreen() { return green; }
    public int getBlue() { return blue; }

    public int getRGB() {
        return (red << 16) | (green << 8) | blue;
    }

    // Predefined color constants
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color RED = new Color(255, 0, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color BLUE = new Color(0, 0, 255);
}
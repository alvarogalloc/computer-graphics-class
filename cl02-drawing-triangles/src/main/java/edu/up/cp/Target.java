package edu.up.cp;

/**
 * Target interface for rendering operations.
 * Defines the contract for image rendering targets that can set pixels,
 * clear the image, save to file, and provide dimensions.
 */
public interface Target {
    void setRGB(int x, int y, Color color);
    void clear(Color color);
    void saveToFile(String filename) throws Exception;
    int getWidth();
    int getHeight();
}
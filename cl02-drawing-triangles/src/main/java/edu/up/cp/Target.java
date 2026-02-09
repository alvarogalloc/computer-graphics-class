package edu.up.cp;

/**
 * Target interface for rendering operations.
 * this is great, then it could be anything, from any image format (no alpha yet) but could with setRGBA
 * but also to a surface like opengl, the screenbuffer, etc. Its just an abstraction to a grid of pixels
 */
public interface Target {
    void setRGB(int x, int y, Color color);
    void clear(Color color);
    void saveToFile(String filename) throws Exception;
    int getWidth();
    int getHeight();
}

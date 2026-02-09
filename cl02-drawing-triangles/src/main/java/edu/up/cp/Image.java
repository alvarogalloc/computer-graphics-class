package edu.up.cp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Image class that implements the Target interface using BufferedImage.
 * Provides functionality to create, manipulate, and save images as JPG files.
 * Uses standard Java AWT BufferedImage and ImageIO for image operations.
 */
public class Image implements Target {
    private final BufferedImage bufferedImage;

    public Image(int width, int height) {
        this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void setRGB(int x, int y, Color color) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            bufferedImage.setRGB(x, y, color.getRGB());
        }
    }

    @Override
    public void clear(Color color) {
        int rgb = color.getRGB();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                bufferedImage.setRGB(x, y, rgb);
            }
        }
    }

    @Override
    public void saveToFile(String filename) throws IOException {
        String extension = filename.toLowerCase().endsWith(".jpg") ? "jpg" : "jpg";
        File outputFile = new File(filename);
        ImageIO.write(bufferedImage, extension, outputFile);
    }

    @Override
    public int getWidth() {
        return bufferedImage.getWidth();
    }

    @Override
    public int getHeight() {
        return bufferedImage.getHeight();
    }
}
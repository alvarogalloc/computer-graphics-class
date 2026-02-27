package edu.up.cg.modifiers;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * ColorInverter
 * it basically does this for every pixel in the region             
 * r =1.0 - r,
 * g =1.0 - g,
 * b =1.0 - b,
 */
public class ColorInverter implements ImageModifier {

  public Image apply(final Image image, final Rectangle2D region) {
    if (image == null || region == null) {
      throw new IllegalArgumentException("Image and region must not be null");
    }

    final int imgW = (int) image.getWidth();
    final int imgH = (int) image.getHeight();

    final int startX = (int) Math.max(0, region.getMinX());
    final int startY = (int) Math.max(0, region.getMinY());
    final int endX = (int) Math.min(imgW, region.getMaxX());
    final int endY = (int) Math.min(imgH, region.getMaxY());

    final PixelReader reader = image.getPixelReader();
    final WritableImage output = new WritableImage(imgW, imgH);
    final PixelWriter writer = output.getPixelWriter();

    // Copy full image first
    writer.setPixels(0, 0, imgW, imgH, reader, 0, 0);

    // Invert only selected region
    for (int y = startY; y < endY; y++) {
      for (int x = startX; x < endX; x++) {

        final Color c = reader.getColor(x, y);

        final Color inverted = new Color(
            1.0 - c.getRed(),
            1.0 - c.getGreen(),
            1.0 - c.getBlue(),
            c.getOpacity() // preserve alpha
        );

        writer.setColor(x, y, inverted);
      }
    }

    return output;
  }
}

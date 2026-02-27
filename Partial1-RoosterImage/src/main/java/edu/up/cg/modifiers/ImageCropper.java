package edu.up.cg.modifiers;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * ImageCropper
 * Uses the constructor of WritableImage, which recieves and copies a region of a buffer into another
 * thus, exactly what we want. Under the hood its just two fors and copying from src to dist
 */
public class ImageCropper implements ImageModifier {

  @Override
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

    final int cropW = endX - startX;
    final int cropH = endY - startY;

    if (cropW <= 0 || cropH <= 0) {
      throw new IllegalArgumentException("Region is outside image bounds");
    }

    final PixelReader reader = image.getPixelReader();

    return new WritableImage(
        reader,
        startX,
        startY,
        cropW,
        cropH);
  }
}

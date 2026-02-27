package edu.up.cg.modifiers;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * ImageRotator
 *
 * rotates the image with options of 90,180 and 270
 *
 * It validates the angle and the selected region, then computes safe bounds inside the image.
 * If the whole image is selected, it creates a new image (swapping width/height for 90°/270°) 
 * If only a region is selected, it copies the original image, clears that region, and rewrites its pixels into rotated coordinates.
 *
 */


public class ImageRotator implements ImageModifier {

  private int angle = 90;

  public void setAngle(final int degrees) {
    if (degrees != 90 && degrees != 180 && degrees != 270) {
      throw new IllegalArgumentException("Angle must be 90, 180, or 270");
    }
    this.angle = degrees;
  }

  @Override
  public Image apply(final Image image, final Rectangle2D region) {

    if (image == null || region == null) {
      throw new IllegalArgumentException("Image and region must not be null");
    }

    final int imgW = (int) image.getWidth();
    final int imgH = (int) image.getHeight();

    final int startX = (int) Math.max(0, region.getMinX());
    final int startY = (int) Math.max(0, region.getMinY());
    final int endX   = (int) Math.min(imgW, region.getMaxX());
    final int endY   = (int) Math.min(imgH, region.getMaxY());

    final int w = endX - startX;
    final int h = endY - startY;

    if (w <= 0 || h <= 0) {
      throw new IllegalArgumentException("Region is outside image bounds");
    }

    final PixelReader reader = image.getPixelReader();

    final boolean whole =
        startX == 0 && startY == 0 &&
        w == imgW && h == imgH;

    // ---- WHOLE IMAGE ROTATION ----
    if (whole) {

      WritableImage rotated;

      if (angle == 90 || angle == 270) {
        rotated = new WritableImage(imgH, imgW);
      } else {
        rotated = new WritableImage(imgW, imgH);
      }

      final PixelWriter writer = rotated.getPixelWriter();

      for (int y = 0; y < imgH; y++) {
        for (int x = 0; x < imgW; x++) {

          final int argb = reader.getArgb(x, y);

          switch (angle) {
            case 90:
              writer.setArgb(imgH - 1 - y, x, argb);
              break;

            case 180:
              writer.setArgb(imgW - 1 - x, imgH - 1 - y, argb);
              break;

            case 270:
              writer.setArgb(y, imgW - 1 - x, argb);
              break;
          }
        }
      }

      return rotated;
    }

    // ---- PARTIAL REGION ROTATION ----
    final WritableImage output = new WritableImage(imgW, imgH);
    final PixelWriter writer = output.getPixelWriter();

    // copy full image first
    writer.setPixels(0, 0, imgW, imgH, reader, 0, 0);

    // clear region to black
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        writer.setArgb(startX + x, startY + y, 0xFF000000);
      }
    }

    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {

        final int srcX = startX + x;
        final int srcY = startY + y;

        int dstX = 0;
        int dstY = 0;

        switch (angle) {

          case 90:
            dstX = startX + (h - 1 - y);
            dstY = startY + x;
            break;

          case 180:
            dstX = startX + (w - 1 - x);
            dstY = startY + (h - 1 - y);
            break;

          case 270:
            dstX = startX + y;
            dstY = startY + (w - 1 - x);
            break;
        }

        if (dstX >= startX && dstX < startX + w &&
            dstY >= startY && dstY < startY + h) {

          writer.setArgb(dstX, dstY, reader.getArgb(srcX, srcY));
        }
      }
    }

    return output;
  }
}

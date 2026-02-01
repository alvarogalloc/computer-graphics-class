package edu.up.cg;
import java.awt.Color;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class SunnyDay {
  public static final double dt = 0.0003;
  public static Color color;

  public static void draw_line(BufferedImage img, int x1, int y1, int x2, int y2, double max_t) {
    for (double t = 0; t < max_t; t += dt) {
      final int x = x1 + (int) ((x2 - x1) * t);
      final int y = y1 + (int) ((y2 - y1) * t);

      img.setRGB(x, y, color.getRGB());
    }
  }

  public static void draw_circle_filled(BufferedImage img, int radius, int centerx, int centery) {
    for (double t = -Math.PI / 2; t < Math.PI / 2; t += dt) {
      final int x = (int) (radius * Math.cos(t));
      final int y = (int) (radius * Math.sin(t));
      draw_line(img, centerx - x, centery + y, centerx + x, centery + y, 1.0);
    }
  }

  public static void main(String[] args) {
    int width = 400;
    int height = 300;
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    int center_sunx = (int) (width / 4);
    int center_suny = (int) (height / 4);
    final int radius = 50;
    color = Color.yellow;
    draw_circle_filled(img, radius, center_sunx, center_suny);

    color = Color.white;
    try {
      File out = new File("sunny_day.png");
      ImageIO.write(img, "JPG", out);
    } catch (Exception ex) {
      System.out.println(ex.getLocalizedMessage());
    }
  }

}

package edu.up.cg;

import java.awt.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Function;

import javax.imageio.ImageIO;

public class SunnyDay {
  public static final double dt = 0.0003;
  public static Color color;

  public static void draw_line(BufferedImage img, int x1, int y1, int x2, int y2, double max_t) {
    for (double t = 0; t < max_t; t += dt) {
      final int x = x1 + (int) ((x2 - x1) * t);
      final int y = y1 + (int) ((y2 - y1) * t);

      if (x >= 0 && x < img.getWidth() &&
          y >= 0 && y < img.getHeight()) {
        img.setRGB(x, y, color.getRGB());
      }
    }
  }

  public static void draw_circle_filled(BufferedImage img, int radius, int centerx, int centery) {
    for (double t = -Math.PI / 2; t < Math.PI / 2; t += dt) {
      final int x = (int) (radius * Math.cos(t));
      final int y = (int) (radius * Math.sin(t));
      draw_line(img, centerx - x, centery + y, centerx + x, centery + y, 1.0);
    }
  }

  public static void fill(BufferedImage img) {
    for (int j = 0; j < img.getHeight(); j++) {
      for (int i = 0; i < img.getWidth(); i++) {
        img.setRGB(i, j, color.getRGB());
      }
    }
  }

  public static void fill_below_fn(BufferedImage img, Function<Double, Double> fn) {
    int w = img.getWidth();
    int h = img.getHeight();

    for (int i = 0; i < w; i++) {
      double x = i; // screen-space x
      double y = fn.apply(x); // math-space height (0..1)

      int sx = i;

      int syFn = (int) (h - 1 - y * h * 0.9);
      int syBase = h - 1;

      draw_line(img, sx, syFn, sx, syBase, 1.0);
    }
  }

  public static void main(String[] args) {
    int width = 1600;
    int height = 1200;
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    color = Color.white;
    fill(img);
    int center_sunx = (int) (width / 4);
    int center_suny = (int) (height / 4);
    final int radius = 150;

    color = Color.red;
    int large_offset = 90;
    draw_line(img, center_sunx, center_suny - radius - large_offset, center_sunx, center_suny + radius + large_offset,
        1.f);
    draw_line(img, center_sunx - radius - large_offset, center_suny, center_sunx + radius + large_offset, center_suny,
        1.f);

    int little_offset = 45;
    int start_little_line_x = (int) ((radius + little_offset) * Math.cos(Math.PI / 4));
    int start_little_line_y = (int) ((radius + little_offset) * Math.sin(Math.PI / 4));
    draw_line(img, center_sunx + start_little_line_x, center_suny + start_little_line_y,
        center_sunx - start_little_line_x, center_suny - start_little_line_y,
        1.f);

    draw_line(img, center_sunx + start_little_line_x, center_suny - start_little_line_y,
        center_sunx - start_little_line_x, center_suny + start_little_line_y,
        1.f);
    color = Color.yellow;
    draw_circle_filled(img, radius, center_sunx, center_suny);

    color = Color.green;
    fill_below_fn(img, x -> 0.3 + 0.05 * Math.cos(x / 50));
    try {
      File out = new File("sunny_day.jpg");
      ImageIO.write(img, "JPG", out);
    } catch (Exception ex) {
      System.out.println(ex.getLocalizedMessage());
    }
  }

}

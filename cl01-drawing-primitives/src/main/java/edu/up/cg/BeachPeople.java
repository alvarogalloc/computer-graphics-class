package edu.up.cg;

import java.awt.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Function;

import javax.imageio.ImageIO;

public class BeachPeople {
  public static final double dt = 0.0003;
  public static Color color;

  public static double determinant_2(int px, int py, int ax, int ay, int bx, int by) {
    return (px - ax) * (by - ay) - (py - ay) * (bx - ax);
  }

  public static void draw_triangle(BufferedImage img, int ax, int ay, int bx, int by, int cx, int cy) {
    /*
     * we will use a bound box iteration
     * were in each pixel we will check if the point is inside the triangle
     * this is a composition of three conditions
     * for each pair of vertices A,B:
     * is left or right from the line that it forms from A to B
     * using the 2d determinant
     */
    int minX = Math.min(ax, Math.min(bx, cx));
    int minY = Math.min(ay, Math.min(by, cy));
    int maxX = Math.max(ax, Math.max(bx, cx));
    int maxY = Math.max(ay, Math.max(by, cy));

    // this is to dont care if i pass an out of bounds triangle
    minX = Math.max(minX, 0);
    minY = Math.max(minY, 0);
    maxX = Math.min(maxX, img.getWidth() - 1);
    maxY = Math.min(maxY, img.getHeight() - 1);
    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {

        double w1 = determinant_2(x, y, ax, ay, bx, by);
        double w2 = determinant_2(x, y, bx, by, cx, cy);
        double w3 = determinant_2(x, y, cx, cy, ax, ay);

        boolean isCounterClockwise = (w1 >= 0 && w2 >= 0 && w3 >= 0);
        boolean isClockwise = (w1 <= 0 && w2 <= 0 && w3 <= 0);

        // it is either all >=1 or all <=0, this removes the need of the order on which
        // we pass the points
        if (isCounterClockwise || isClockwise) {
          img.setRGB(x, y, color.getRGB());
        }
      }
    }
  }

  public static void draw_rect(BufferedImage img, int x, int y, int w, int h) {
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        if (i >= 0 && i < img.getWidth() && j >= 0 && j < img.getHeight()) {
          img.setRGB(i, j, color.getRGB());
        }
      }
    }
  }

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
    color = Color.black;
    for (int y = 0; y < img.getHeight(); y++) {
      int g = 255 - ((y * 255) / img.getHeight());
      for (int x = 0; x < img.getWidth(); x++) {
        img.setRGB(x, y, new Color(255, g, 0).getRGB());
      }
    }

    /*
     * for (int i = 0; i < 3; i++) {
     * int ax = 200 + i * 450;
     * int bx = 300 + i * 450;
     * int cx = 400 + i * 450;
     * int ay = 1000;
     * int by = 700;
     * int cy = 1000;
     * 
     * draw_triangle(img, ax, ay, bx, by, cx, cy);
     * draw_triangle(img, ax - 20, img.getHeight() - ay + 400, bx, img.getHeight() -
     * by + 400, cx + 20,
     * img.getHeight() - cy + 400);
     * }
     */

    for (int i = 0; i < 3; i++) {
      int centerX = 400 + i * 450;
      int topy = 900;
      int h_first = 400;
      int overlap = 300;

      // torso
      draw_triangle(img,
          centerX, topy,
          centerX - 100, topy - h_first,
          centerX + 100, topy - h_first);
      draw_triangle(img,
          centerX, topy - overlap,
          centerX - 100, topy + h_first - overlap,
          centerX + 100, topy + h_first - overlap);
      // legs
      draw_rect(img, centerX - 60, topy + h_first - overlap, 20, 50);
      draw_rect(img, centerX + 40, topy + h_first - overlap, 20, 50);

      // neck
      draw_rect(img, centerX - 10, topy - h_first - 50, 20, 50);
      draw_circle_filled(img, 70, centerX, topy - h_first - 120);
      draw_circle_filled(img, 30, centerX+70, topy - h_first - 170);
    }
    try {
      File out = new File("beach_people.jpg");
      ImageIO.write(img, "JPG", out);
    } catch (Exception ex) {
      System.out.println(ex.getLocalizedMessage());
    }
  }

}

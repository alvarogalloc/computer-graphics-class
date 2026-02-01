package edu.up.cg;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Clock {
  static final double dt = 0.006f;

  public static void draw_circle(BufferedImage img, int radius, int centerx, int centery) {
    for (double t = 0; t < 2 * Math.PI; t += dt) {
      final int x = centerx + (int) (radius * Math.cos(t));
      final int y = centery + (int) (radius * Math.sin(t));
      img.setRGB(x, y, Color.white.getRGB());
    }

  }

  public static void draw_line(BufferedImage img, int x1, int y1, int x2, int y2, double max_t) {
    for (double t = 0; t < max_t; t += dt) {
      final int x = x1 + (int) ((x2 - x1) * t);
      final int y = y1 + (int) ((y2 - y1) * t);

      img.setRGB(x, y, Color.white.getRGB());
    }
  }

  static double get_hour_angle(int hour) {
    final double angle_step = (2 * Math.PI / 12);
    return -Math.PI/2 + (hour) * (angle_step);
  }

  static double get_minute_angle(int minute) {
    final double angle_step = (2 * Math.PI / 60);
    return -Math.PI/2 + (minute) * (angle_step);
  }

  public static void main(String[] args) {
    int width = 800;
    int height = 600;
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    int centerx = (int) (width / 2);
    int centery = (int) (height / 2);
    final int radius = 250;
    draw_circle(img, radius, centerx, centery);
    draw_circle(img, radius + 1, centerx, centery);

    // clock should say it is 10:05
    final double angle_step = (2 * Math.PI / 12);

    Scanner in = new Scanner(System.in);
    double hour_angle = get_hour_angle(in.nextInt());
    double minute_angle = get_minute_angle(in.nextInt());
    in.close();


    draw_line(img, centerx, centery, (int) (centerx + radius * Math.cos(minute_angle)),
        (int) (centery + radius * Math.sin(minute_angle)), 0.75);

    draw_line(img, centerx, centery, (int) (centerx + radius * Math.cos(hour_angle)),
        (int) (centery + radius * Math.sin(hour_angle)), 0.5);


    for (int i = 0; i<12;i++) {
      double inner_radius = radius*0.8;
      int x = (int)(centerx+ inner_radius*Math.cos(i*angle_step));
      int y = (int)(centery+ inner_radius*Math.sin(i*angle_step));
      img.setRGB(x, y, Color.white.getRGB());
    }

    try {
      File out = new File("clock.png");
      ImageIO.write(img, "JPG", out);
    } catch (Exception ex) {
      System.out.println(ex.getLocalizedMessage());
    }
  }

}

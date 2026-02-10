package edu.up.cp;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        try {
            Image image = new Image(2000, 2000);
            RenderData vertices = new RenderData();
            int v1 = vertices.addVertex(0, image.getHeight(), Color.RED);
            int v2 = vertices.addVertex(image.getWidth() / 2, 0, Color.GREEN);
            int v3 = vertices.addVertex(image.getWidth(), image.getHeight(), Color.BLUE);
            Triangle classTriangle = new Triangle(v1, v2, v3, vertices);
            classTriangle.render(image);

            image.saveToFile("triangle.tiff");

            System.out.println("Triangle rendering completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error during triangle rendering: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package edu.up.cp;

/**
 * Main class for triangle rendering demonstration.
 * Creates various triangles with different colors and positions,
 * then saves the results as JPG images using BufferedImage and ImageIO.
 */
public class Main {
    public static void main(String[] args) {
        try {
            Image image = new Image(500, 500);
            RenderData vertices = new RenderData();
            
            // First small triangle with different colors at each vertex
            vertices.addVertex(0, 0, Color.BLACK);
            vertices.addVertex(0, 50, Color.RED);
            vertices.addVertex(50, 50, Color.GREEN);
            Triangle t1 = new Triangle(0, 1, 2, vertices);
            t1.render(image);

            // Second triangle in green
            vertices.addVertex(250, 300, Color.GREEN);
            vertices.addVertex(400, 400, Color.GREEN);
            vertices.addVertex(200, 400, Color.GREEN);
            Triangle t2 = new Triangle(3, 4, 5, vertices);
            t2.render(image);

            // Third triangle mixing vertices
            Triangle t3 = new Triangle(0, 1, 5, vertices);
            t3.render(image);
            image.saveToFile("triangles.jpg");

            // Draw vertices as colored dots
            image.clear(Color.BLUE);
            vertices.drawVertices(image, 5);
            image.saveToFile("vertices.jpg");

            // Large triangle with different colors at corners
            image.clear(Color.BLACK);
            int v1 = vertices.addVertex(0, image.getHeight(), Color.RED);
            int v2 = vertices.addVertex(image.getWidth() / 2, 0, Color.GREEN);
            int v3 = vertices.addVertex(image.getWidth(), image.getHeight(), Color.BLUE);
            Triangle classTriangle = new Triangle(v1, v2, v3, vertices);
            classTriangle.render(image);

            image.saveToFile("class_triangle.jpg");

            System.out.println("Triangle rendering completed successfully!");
            System.out.println("Generated files: triangles.jpg, vertices.jpg, class_triangle.jpg");
            
        } catch (Exception e) {
            System.err.println("Error during triangle rendering: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
package edu.up.cp;

import java.util.ArrayList;
import java.util.List;

/**
 * RenderData class manages vertex and color data for triangle rendering.
 * Maintains two parallel buffers: one for vertex coordinates and one for colors.
 * Provides methods to add vertices, retrieve vertex data, and draw vertices as points.
 */
public class RenderData {
    private final List<Color> colorBuffer;
    private final List<Integer> vertexBuffer;

    public RenderData() {
        this.colorBuffer = new ArrayList<>();
        this.vertexBuffer = new ArrayList<>();
    }

    /**
     * Adds a vertex with its associated color.
     * Returns the ID of the newly added vertex.
     */
    public int addVertex(int x, int y, Color color) {
        vertexBuffer.add(x);
        vertexBuffer.add(y);
        colorBuffer.add(color);
        return vertexCount() - 1;
    }

    /**
     * Retrieves the coordinates of a vertex by its ID.
     */
    public int[] getVertex(int vertexId) {
        assertInBounds(vertexId);
        return new int[]{
            vertexBuffer.get(vertexId * 2),
            vertexBuffer.get(vertexId * 2 + 1)
        };
    }

    /**
     * Retrieves the color of a vertex by its ID.
     */
    public Color getColor(int vertexId) {
        assertInBounds(vertexId);
        return colorBuffer.get(vertexId);
    }

    /**
     * Updates the position of an existing vertex.
     */
    public void setVertex(int vertexId, int x, int y) {
        assertInBounds(vertexId);
        vertexBuffer.set(vertexId * 2, x);
        vertexBuffer.set(vertexId * 2 + 1, y);
    }

    /**
     * Updates the color of an existing vertex.
     */
    public void setColor(int vertexId, Color color) {
        assertInBounds(vertexId);
        colorBuffer.set(vertexId, color);
    }

    /**
     * Returns the total number of vertices.
     */
    public int vertexCount() {
        return colorBuffer.size();
    }

    /**
     * Draws all vertices as colored circles on the target.
     */
    public void drawVertices(Target target, int radius) {
        for (int i = 0; i < colorBuffer.size(); i++) {
            int centerX = vertexBuffer.get(i * 2);
            int centerY = vertexBuffer.get(i * 2 + 1);
            Color color = colorBuffer.get(i);

            int radiusSquared = radius * radius;
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (dx * dx + dy * dy <= radiusSquared) {
                        target.setRGB(centerX + dx, centerY + dy, color);
                    }
                }
            }
        }
    }

    private void assertInBounds(int vertexId) {
        if (vertexId >= colorBuffer.size() || vertexId < 0) {
            throw new IndexOutOfBoundsException(
                "Vertex ID " + vertexId + " does not exist, max is " + (colorBuffer.size() - 1)
            );
        }
    }
}
package edu.up.cp;

/**
 * Triangle class for rendering filled triangles with interpolated colors.
 * Uses edge function and barycentric coordinates for rasterization.
 * Supports color interpolation across the triangle surface.
 */
public class Triangle {
    private final int v1Id;
    private final int v2Id;
    private final int v3Id;
    private final RenderData renderData;

    public Triangle(int v1, int v2, int v3, RenderData data) {
        if (data == null) {
            throw new IllegalArgumentException("RenderData cannot be null");
        }
        this.v1Id = v1;
        this.v2Id = v2;
        this.v3Id = v3;
        this.renderData = data;
    }

    /**
     * Edge function for determining which side of a line a point lies on.
     * Returns positive if point is on one side, negative on the other, zero on the line.
     */
    private static int edgeFunction(int x1, int y1, int x2, int y2, int x, int y) {
        return (y2 - y1) * (x - x1) - (x2 - x1) * (y - y1);
    }

    /**
     * Calculates barycentric coordinates for a point within a triangle.
     * Returns weights (w1, w2, w3) that sum to 1 for points inside the triangle.
     */
    private static float[] barycentric(int x1, int y1, int x2, int y2, int x3, int y3, int x, int y) {
        float denom = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);

        if (denom == 0.0f) {
            return new float[]{0.0f, 0.0f, 0.0f};
        }

        float w1Num = (y2 - y3) * (x - x3) + (x3 - x2) * (y - y3);
        float w2Num = (y3 - y1) * (x - x3) + (x1 - x3) * (y - y3);
        float w3Num = denom - w1Num - w2Num;

        return new float[]{w1Num / denom, w2Num / denom, w3Num / denom};
    }

    /**
     * Interpolates color at a specific point using barycentric coordinates.
     * Acts as a fragment shader to compute the final pixel color.
     */
    private Color interpolateColor(int x, int y) {
        int[] v1 = renderData.getVertex(v1Id);
        int[] v2 = renderData.getVertex(v2Id);
        int[] v3 = renderData.getVertex(v3Id);

        float[] weights = barycentric(v1[0], v1[1], v2[0], v2[1], v3[0], v3[1], x, y);

        Color c1 = renderData.getColor(v1Id);
        Color c2 = renderData.getColor(v2Id);
        Color c3 = renderData.getColor(v3Id);

        float r = c1.getRed() * weights[0] + c2.getRed() * weights[1] + c3.getRed() * weights[2];
        float g = c1.getGreen() * weights[0] + c2.getGreen() * weights[1] + c3.getGreen() * weights[2];
        float b = c1.getBlue() * weights[0] + c2.getBlue() * weights[1] + c3.getBlue() * weights[2];

        return new Color(
            Math.round(r),
            Math.round(g),
            Math.round(b)
        );
    }

    /**
     * Renders the triangle to the specified target using rasterization.
     * Fills the triangle with interpolated colors.
     */
    public void render(Target target) {
        int[] v1 = renderData.getVertex(v1Id);
        int[] v2 = renderData.getVertex(v2Id);
        int[] v3 = renderData.getVertex(v3Id);

        // Early exit for malformed triangles
        int area2 = edgeFunction(v1[0], v1[1], v2[0], v2[1], v3[0], v3[1]);
        if (area2 == 0) {
            return;
        }

        // Bounding box
        int minX = Math.min(v1[0], Math.min(v2[0], v3[0]));
        int minY = Math.min(v1[1], Math.min(v2[1], v3[1]));
        int maxX = Math.max(v1[0], Math.max(v2[0], v3[0]));
        int maxY = Math.max(v1[1], Math.max(v2[1], v3[1]));

        // Determine winding order for consistent inside/outside testing
        boolean v1v2InsideIsPositive = edgeFunction(v1[0], v1[1], v2[0], v2[1], v3[0], v3[1]) >= 0;
        boolean v2v3InsideIsPositive = edgeFunction(v2[0], v2[1], v3[0], v3[1], v1[0], v1[1]) >= 0;
        boolean v3v1InsideIsPositive = edgeFunction(v3[0], v3[1], v1[0], v1[1], v2[0], v2[1]) >= 0;

        // Rasterize
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                // Test which side of each edge the point is on
                int testV1V2 = edgeFunction(v1[0], v1[1], v2[0], v2[1], x, y);
                int testV2V3 = edgeFunction(v2[0], v2[1], v3[0], v3[1], x, y);
                int testV3V1 = edgeFunction(v3[0], v3[1], v1[0], v1[1], x, y);

                boolean insideV1V2 = (v1v2InsideIsPositive && testV1V2 >= 0) ||
                                   (!v1v2InsideIsPositive && testV1V2 <= 0);
                boolean insideV2V3 = (v2v3InsideIsPositive && testV2V3 >= 0) ||
                                   (!v2v3InsideIsPositive && testV2V3 <= 0);
                boolean insideV3V1 = (v3v1InsideIsPositive && testV3V1 >= 0) ||
                                   (!v3v1InsideIsPositive && testV3V1 <= 0);

                if (insideV1V2 && insideV2V3 && insideV3V1) {
                    Color pixelColor = interpolateColor(x, y);
                    target.setRGB(x, y, pixelColor);
                }
            }
        }
    }
}
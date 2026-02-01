package edu.up.cg.ui;

import java.util.Map;
import java.util.Objects;
import javafx.geometry.Bounds;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

class Drawable {
  /*
   * this class is to have a map of FigureName -> SvgDrawable from javafx
   * i want to return given the name, the figure of it strecthed to have all the
   * available width
   *
   * supported figures:
   * - circle
   * - rectangle
   * - semicircle
   * - square
   * - triangle
   * - Pentagon
   */
  private static Map<String, SVGPath> drawableMap = Map.of(
      "Circle", new SVGPath() {
        {
          setContent("M 50, 0 A 50,50 0 1,0 50,100 A 50,50 0 1,0 50,0 Z");
        }
      },
      "Square", new SVGPath() {
        {
          setContent("M 0,0 L 100,0 L 100,100 L 0,100 Z");
        }
      },
      "Rectangle", new SVGPath() {
        {
          setContent("M 0,0 L 150,0 L 150,100 L 0,100 Z");
        }
      },
      "Semicircle", new SVGPath() {
        {
          setContent("M 0,100 A 50,50 0 1,1 100,100 L 100,100 L 0,100 Z");
        }
      },
      "Triangle", new SVGPath() {
        {
          setContent("M 50,0 L 100,100 L 0,100 Z");
        }
      },
      "Pentagon", new SVGPath() {
        {
          setContent("""
                  M 50 0
                  L 97.55 34.55
                  L 79.39 90.45
                  L 20.61 90.45
                  L 2.45 34.55
                  Z
              """);
        }
      });

  public static SVGPath getDrawable(String figureName, Double targetWidth) {
    SVGPath original = drawableMap.get(figureName);
    Objects.requireNonNull(original);

    // 1. Create a copy of the SVGPath.
    // This prevents "Node is already a child of another" errors and ensures
    // we don't pile transforms onto the original static object.
    SVGPath p = new SVGPath();
    p.setContent(original.getContent());
    p.setStyle("-fx-fill: black;"); // Ensure it has a color

    Bounds b = p.getLayoutBounds();

    // 2. Calculate the desired width (2/3 of target) and the resulting scale
    double desiredFigureWidth = targetWidth * (2.0 / 3.0);
    double scale = desiredFigureWidth / b.getWidth();

    // 3. Calculate the X offset to center the shape
    // (Available Width - Actual Figure Width) / 2
    double xOffset = (targetWidth - desiredFigureWidth) / 2.0;

    // 4. Apply Transforms in order
    p.getTransforms().clear();

    // Step A: Move the shape's top-left corner to (0,0)
    p.getTransforms().add(new Translate(-b.getMinX(), -b.getMinY()));

    // Step B: Scale it
    p.getTransforms().add(new Scale(scale, scale));

    // Step C: Move the scaled shape to the center
    p.getTransforms().add(new Translate(xOffset / scale, 0));
    // Note: Since we apply Translate *after* Scale in the list, some logic might
    // suggest
    // the translation value is scaled. However, the simplest way to visualize this
    // stack
    // in JavaFX is that the `Translate` adds to the coordinate position.
    // Actually, to be safer and avoid matrix math confusion, we can simply apply
    // the centering translation via the layout property or a distinct translate
    // that isn't nested. But the robust "3-step" pipeline is:
    // [Translate to Origin] -> [Scale] -> [Translate to Center]

    // Let's refine the transform list to be 100% predictable:
    p.getTransforms().clear();
    p.getTransforms().addAll(
        new Translate(xOffset, 0), // 3. Move to calculated center (Parent coords)
        new Scale(scale, scale), // 2. Scale up
        new Translate(-b.getMinX(), -b.getMinY()) // 1. Normalize to 0,0 (Local coords)
    );
    // JavaFX applies transforms in the order they are in the list (T1 * T2 * T3).
    // T1(Translate Center) * T2(Scale) * T3(Translate Origin) * Point
    // This correctly scales the shape and places it at the calculated center
    // offset.

    return p;
  }

}

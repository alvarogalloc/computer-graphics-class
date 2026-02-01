package edu.up.cg.calc;

import java.util.Map;
import java.util.function.Function;

public class PerimeterCalculator {

  private static final Map<String, Function<Map<String, Double>, Double>> PERIMETER = Map.of(
      "Square", in -> 4 * in.get("side"),

      "Rectangle", in -> 2 * (in.get("width") + in.get("height")),

      "Triangle", in -> in.get("sideA") + in.get("sideB") + in.get("sideC"),

      "Circle", in -> 2 * Math.PI * in.get("radius"),

      "Pentagon", in -> 5 * in.get("side"),

      "Semicircle", in -> Math.PI * in.get("radius") + 2 * in.get("radius"));

  public static Double calculate(String type, Map<String, Double> inputs) {
    try {
      return PERIMETER.get(type).apply(inputs);
    } catch (Exception e) {
      return 0.0;
    }
  }
}

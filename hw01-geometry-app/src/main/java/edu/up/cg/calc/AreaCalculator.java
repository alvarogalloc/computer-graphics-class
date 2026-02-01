package edu.up.cg.calc;

import java.util.Map;
import java.util.function.Function;

public class AreaCalculator {

  private static final Map<String, Function<Map<String, Double>, Double>> AREA = Map.of(
      "Square", in -> Math.pow(in.get("side"), 2),

      "Rectangle", in -> in.get("width") * in.get("height"),

      "Triangle", in -> (in.get("base") * in.get("height")) / 2,

      "Circle", in -> Math.PI * Math.pow(in.get("radius"), 2),

      "Pentagon", in -> (5 * in.get("side") * in.get("apothem")) / 2,

      "Semicircle", in -> (Math.PI * Math.pow(in.get("radius"), 2)) / 2);

  public static Double calculate(String type, Map<String, Double> inputs) {
    try {
      return AREA.get(type).apply(inputs);
    } catch (Exception e) {
      return 0.0;
    }
  }
}

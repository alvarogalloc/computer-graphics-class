package edu.up.cp;
import java.io.File;
import java.io.FileWriter;

public class Main {
  public static void main(String[] args) {
    try {

      FileWriter writer = new FileWriter(new File("diagonal.svg"));
      writer.write(makeDiagonal());
      writer.close();
      writer = new FileWriter(new File("beach_day.svg"));
      writer.write(makeBeachDay());
      writer.close();
    } catch (Exception e) {
      System.out.println("could not write to file: " + e.toString());
    }

  }

  public static String makeDiagonal() {
    Integer width = 400;
    Integer height = 300;
    return String.format(
        "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%1$d\" height=\"%2$d\">" +
            "    <polyline points=\"0 0 %1$d %2$d %1$d 0\" stroke=\"blue\" fill=\"blue\"></polyline>" +
            "    <polyline points=\"0 0 %1$d %2$d 0 %1$d \" stroke=\"red\" fill=\"red\"></polyline>" +
            "</svg>",
        width, height);

  }

  public static String makeBeachDay() {
    Integer width = 400;
    Integer height = 300;
    String sunFragment = "<g transform=\"translate(30, 30)\">" +
        "   <path d=\"M 50 0 V 100 \" stroke=\"#BA7B86\"/>" +
        "   <path d=\"M 0 50 H 100\" stroke=\"#BA7B86\"/>" +
        "   <path d=\"M 15 15 L 85 85 \" stroke=\"#BA7B86\"/>" +
        "   <path d=\"M 15 85 L 85 15\" stroke=\"#BA7B86\"/>" +
        "   <circle cx=\"50\" cy=\"50\" r=\"40\" fill=\"yellow\"/>" +
        " </g>";
    int sineHeigth = 150;
    String sineFragment = plotSineWave(width, sineHeigth, 15.0, .1, 0);
    return String.format(
        "<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
            "    %s\n" +
            "    <g transform=\"translate(0, %d)\">%s\n</g>" +
            "</svg>",
        width, height, sunFragment, height - sineHeigth, sineFragment);
  }

  public static String plotSineWave(int width, int height, double amplitude, double frequency, double phase) {
    StringBuilder pathData = new StringBuilder();

    double midY = height / 2.0;
    pathData.append(String.format("M 0 %f ", midY));

    for (int x = 0; x <= width; x++) {
      double y = midY + amplitude * Math.sin((x * frequency) + phase);
      pathData.append(String.format("L %d %f ", x, y));
    }

    // add the bottom right and left to close the path and make the figure filled
    pathData.append(String.format("L %d %d ", width, height));
    pathData.append(String.format("L 0 %d ", height));
    pathData.append("Z");

    // colors extracted with eyedropper
    return String.format(
        "<path d=\"%s\" fill=\"#00FF01\" stroke=\"#00FF01\" stroke-width=\"2\" />\n",
        pathData.toString());
  }
}

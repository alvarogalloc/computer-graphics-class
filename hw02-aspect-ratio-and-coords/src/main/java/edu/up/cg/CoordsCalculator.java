package edu.up.cg;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class CoordsCalculator extends Application {

  private static final int CANVAS_SIZE = 400;

  @Override
  public void start(Stage stage) {
    TabPane tabs = new TabPane(
        new Tab("Cartesian → Polar", createCartesianToPolarSection()),
        new Tab("Polar → Cartesian", createPolarToCartesianSection()),
        new Tab("Polar Rose", createPolarRoseSection())
    );

    tabs.getTabs().forEach(t -> t.setClosable(false));

    Scene scene = new Scene(tabs, 520, 600);
    stage.setTitle("Coordinate Tools");
    stage.setScene(scene);
    stage.show();
  }

  private VBox createCartesianToPolarSection() {
    TextField xField = new TextField();
    xField.setPromptText("x");

    TextField yField = new TextField();
    yField.setPromptText("y");

    Label result = new Label();

    Button convertBtn = new Button("Convert");
    convertBtn.setOnAction(e ->
        result.setText(cartesianToPolar(xField.getText(), yField.getText()))
    );

    VBox box = new VBox(10,
        new Label("Cartesian → Polar"),
        xField,
        yField,
        convertBtn,
        result
    );

    box.setPadding(new Insets(20));
    return box;
  }

  private VBox createPolarToCartesianSection() {
    TextField rField = new TextField();
    rField.setPromptText("r");

    TextField thetaField = new TextField();
    thetaField.setPromptText("θ (radians)");

    Label result = new Label();

    Button convertBtn = new Button("Convert");
    convertBtn.setOnAction(e ->
        result.setText(polarToCartesian(rField.getText(), thetaField.getText()))
    );

    VBox box = new VBox(10,
        new Label("Polar → Cartesian"),
        rField,
        thetaField,
        convertBtn,
        result
    );

    box.setPadding(new Insets(20));
    return box;
  }

  private VBox createPolarRoseSection() {
    TextField aField = new TextField("100");
    aField.setPromptText("a");

    TextField kField = new TextField("4");
    kField.setPromptText("k");

    TextField y0Field = new TextField("0");
    y0Field.setPromptText("y₀");

    Canvas canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
    GraphicsContext gc = canvas.getGraphicsContext2D();

    Button drawBtn = new Button("Draw");
    drawBtn.setOnAction(e -> {
      try {
        double a = Double.parseDouble(aField.getText());
        int k = Integer.parseInt(kField.getText());
        double y0 = Double.parseDouble(y0Field.getText());
        drawPolarRose(gc, a, k, y0);
      } catch (NumberFormatException ignored) {}
    });

    VBox box = new VBox(10,
        new Label("r(φ) = a cos(kφ + y₀)"),
        aField,
        kField,
        y0Field,
        drawBtn,
        canvas
    );

    box.setPadding(new Insets(20));
    box.setAlignment(Pos.TOP_CENTER);
    return box;
  }

  private String cartesianToPolar(String xText, String yText) {
    try {
      double x = Double.parseDouble(xText);
      double y = Double.parseDouble(yText);

      double r = Math.sqrt(x * x + y * y);
      double theta = Math.atan2(y, x);

      return String.format("r = %.2f\nθ = %.2f rad", r, theta);
    } catch (NumberFormatException e) {
      return "Invalid input.";
    }
  }

  private String polarToCartesian(String rText, String thetaText) {
    try {
      double r = Double.parseDouble(rText);
      double theta = Double.parseDouble(thetaText);

      double x = r * Math.cos(theta);
      double y = r * Math.sin(theta);

      return String.format("x = %.2f\ny = %.2f", x, y);
    } catch (NumberFormatException e) {
      return "Invalid input.";
    }
  }

  private void drawPolarRose(GraphicsContext gc, double a, int k, double y0) {
    gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
    gc.setStroke(Color.DARKMAGENTA);
    gc.setLineWidth(2);

    double cx = CANVAS_SIZE / 2.0;
    double cy = CANVAS_SIZE / 2.0;

    double prevX = cx;
    double prevY = cy;

    for (double phi = 0; phi <= 2 * Math.PI; phi += 0.01) {
      double r = a * Math.cos(k * phi + y0);
      double x = r * Math.cos(phi);
      double y = r * Math.sin(phi);

      double dx = cx + x;
      double dy = cy - y;

      gc.strokeLine(prevX, prevY, dx, dy);
      prevX = dx;
      prevY = dy;
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}

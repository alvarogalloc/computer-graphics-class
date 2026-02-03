package edu.up.cg;

import java.io.File;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AspectRatioCalculator extends Application {

  @Override
  public void start(Stage stage) {

    Label infoLabel = new Label("Upload an image to calculate its aspect ratio.");
    Label resultLabel = new Label();

    ImageView imageView = new ImageView();
    imageView.setFitWidth(300);
    imageView.setPreserveRatio(true);

    Button uploadButton = new Button("Upload Image");

    uploadButton.setOnAction(e -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Select an Image (png or jpg)");
      fileChooser.getExtensionFilters().add(
          new FileChooser.ExtensionFilter(
              "Image Files", "*.png", "*.jpg"));
      // i think at least javafx should support this two

      File file = fileChooser.showOpenDialog(stage);
      if (file != null) {
        Image image = new Image(file.toURI().toString());

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        int gcd = gcd(width, height);
        int ratioW = width / gcd;
        int ratioH = height / gcd;

        imageView.setImage(image);
        resultLabel.setText(
            "Width: " + width +
                "\nHeight: " + height +
                "\nAspect Ratio: " + ratioW + ":" + ratioH);
      }
    });

    VBox layout = new VBox(10, infoLabel, uploadButton, imageView, resultLabel);
    layout.setPadding(new Insets(20));

    stage.setScene(new Scene(layout, 360, 450));
    stage.setTitle("Aspect Ratio - Alvaro Gallo");
    stage.show();
  }

  // Helper method to calculate GCD
  // recursive apporach
  private int gcd(int a, int b) {
    if (b == 0)
      return a;
    return gcd(b, a % b);
  }

  public static void main(String[] args) {
    launch(args);
  }
}

package edu.up.cg;

import java.io.File;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class App extends Application {

  private ImagePanel panel;

  @Override
  public void start(Stage stage) {
    stage.setScene(makeScene(stage));
    stage.show();
  }

  public Scene makeScene(Stage stage) {
    var div = new StackPane();
    ToolMenu toolMenu = new ToolMenu();
    this.panel = new ImagePanel("placeholder-image.jpg");
    toolMenu.getImageButton().setOnAction(event -> {

      FileChooser fileChooser = new FileChooser();
      File imageFile = fileChooser.showOpenDialog(stage);
      var path = imageFile.toURI().toString();
      this.panel = new ImagePanel(path);
    });
    div.getChildren().addAll(toolMenu, panel);
    div.setAlignment(Pos.TOP_CENTER);
    var scene = new Scene(div, 640, 480);

    return scene;
  }

  public static void main(String[] args) {
    launch();
  }

}

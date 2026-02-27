package edu.up.cg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.up.cg.modifiers.*;
import edu.up.cg.ui.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/*
 * Application class
 * - this is the responsible of the broader management of the editor
 * - it maintains references to the two main parts of the app
 *   - ToolMenu: the toolbar with the actions to do with the image
 *   - ImagePanel: the editor 'view', it show the image and lets me select a rectangular region
 *    - this is the way editors mostly work, so no need of manual xy inputs for the inputs
 *
 *  - it has a method for binding the different actions of the toolbal
 *    - separating ui and logic for different classes
 *  - also it has the saveImage method that is for saving the edited image to hard drive
 *
 *  - the most important decision here was to enforce to load a file on startup, so i avoid to 
 *    even think about a state where no image is loaded, so no need to 'dosibale' any button or
 *    to put a placeholder image
 * */
public class App extends Application {

  public static void main(final String[] args) {
    launch();
  }

  private ImagePanel panel;
  private ToolMenu toolMenu;

  private String currentImageName;

  @Override
  public void start(final Stage stage) {
    stage.setResizable(false);

    stage.setScene(makeScene(stage));
    stage.show();

    toolMenu.getImageButton().fire();
  }

  private Scene makeScene(final Stage stage) {
    final var div = new VBox();
    toolMenu = new ToolMenu();
    toolMenu.getImageButton().setOnAction(event -> {
      final var fileChooser = new FileChooser();
      fileChooser.setTitle("Open Image:");
      fileChooser.getExtensionFilters().add(
          new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
      final File imageFile = fileChooser.showOpenDialog(stage);

      if (imageFile == null) {
        // if no image is selected then exit
        if (!this.panel.hasImage()) {
          Platform.exit();
        }
        // if not, keep the last one
        return;
      }

      final var path = imageFile.toURI().toString();
      // save for setInitialFileName
      // without extension
      this.currentImageName = imageFile.getName();
      this.panel.setImage(new Image(path));
    });

    this.panel = new ImagePanel();
    setActionButtons();
    // the panel is all the rest of the window
    VBox.setVgrow(panel, Priority.ALWAYS);
    HBox.setHgrow(panel, Priority.ALWAYS);
    div.getChildren().addAll(toolMenu, panel);
    div.setAlignment(Pos.TOP_CENTER);

    final var scene = new Scene(div, 320 * 3, 240 * 3);
    return scene;
  }

  private void applyAction(final ImageModifier action) {

    this.panel.setImage(action.apply(this.panel.getImage(), this.panel.getRegion()));
  }

  // this is all the logic for the buttons, the filters are applied through an
  // abstract one
  // todo make a stack of modifications to implement 'undo'
  private void setActionButtons() {
    this.toolMenu.getInvertButton().setOnAction(ev -> {
      applyAction(new ColorInverter());
    });
    this.toolMenu.getCropButton().setOnAction(ev -> {
      applyAction(new ImageCropper());
    });

    this.toolMenu.getRotateButton().setOnAction(e -> {
      final int deg = toolMenu.getRotateDegrees();
      final var rot = new ImageRotator();
      rot.setAngle(360-deg);
      panel.setImage(
          rot.apply(panel.getImage(), panel.getRegion()));
    });

    toolMenu.getSaveButton().setOnAction(e -> {
      saveImage(this.panel.getImage(),
          toolMenu.getScene().getWindow());
    });
  }

  // saves the image to disk
  // only png and jpeg
  private void saveImage(final Image image, final Window owner) {
    if (image == null)
      return;

    final var fileChooser = new FileChooser();
    fileChooser.setTitle("Save Image:");

    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("PNG Image", "*.png"),
        new FileChooser.ExtensionFilter("JPG Image", "*.jpg", "*.jpeg"));

    String baseName = "image";
    if (this.currentImageName != null && this.currentImageName.contains(".")) {
      baseName = this.currentImageName.substring(
          0,
          this.currentImageName.lastIndexOf('.'));
    }

    fileChooser.setInitialFileName(baseName + "-modified.png");

    final File file = fileChooser.showSaveDialog(owner);
    if (file == null)
      return;

    String name = file.getName().toLowerCase();
    System.out.println(name);
    String ext = (name.endsWith(".jpg") || name.endsWith(".jpeg")) ? "jpg" : "png";

    try {
      final BufferedImage buffered = SwingFXUtils.fromFXImage(image, null);
      ImageIO.write(buffered, ext, file);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

}

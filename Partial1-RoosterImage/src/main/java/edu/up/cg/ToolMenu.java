package edu.up.cg;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;

public class ToolMenu extends ToolBar {

  private final Button imageButton;
  private final Button cropButton;
  private final Button invertButton;
  private final Button rotateButton;

  public ToolMenu() {
    String imageSvg = "m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 0 0 1.5-1.5V6a1.5 1.5 0 0 0-1.5-1.5H3.75A1.5 1.5 0 0 0 2.25 6v12a1.5 1.5 0 0 0 1.5 1.5Zm10.5-11.25h.008v.008h-.008V8.25Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Z";
    String cropSvg = "M7,17V1H5v4H1v2h4v10c0,1.1,0.9,2,2,2h10v4h2v-4h4v-2H7z";
    String invertSvg = "M12,2C6.48,2,2,6.48,2,12s4.48,10,10,10s10-4.48,10-10S17.52,2,12,2z M12,20V4c4.41,0,8,3.59,8,8S16.41,20,12,20z";
    String rotateSvg = "M12,5V1L7,6l5,5V7c3.31,0,6,2.69,6,6s-2.69,6-6,6s-6-2.69-6-6H4c0,4.42,3.58,8,8,8s8-3.58,8-8S16.42,5,12,5z";

    // Initialize buttons
    imageButton = createIconButton(imageSvg, "load image");
    cropButton = createIconButton(cropSvg, "crop to region");
    invertButton = createIconButton(invertSvg, "invert colors");
    rotateButton = createIconButton(rotateSvg, "rotate region");

    // Set default states for when we haven't loaded an image
    cropButton.setDisable(true);
    rotateButton.setDisable(true);

    // Invert colors is always enabled. We enforce this by ignoring disable
    // requests.
    invertButton.setDisable(false);
    invertButton.disableProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal) {
        invertButton.setDisable(false);
      }
    });

    // Add buttons to the ToolBar
    this.getItems().addAll(imageButton, cropButton, invertButton, rotateButton);
  }

  /**
   * Helper method to generate buttons with SVG graphics and tooltips.
   */
  private Button createIconButton(String svgPathData, String tooltipText) {
    SVGPath path = new SVGPath();
    path.setContent(svgPathData);
    path.setFill(Color.DARKSLATEGRAY);

    Button button = new Button();
    button.setGraphic(path);
    button.setTooltip(new Tooltip(tooltipText));

    return button;
  }

  /**
   * Enables or disables the 'crop to region' button.
   */
  public void setCropEnabled(boolean enabled) {
    cropButton.setDisable(!enabled);
  }

  /**
   * Enables or disables the 'rotate region' button.
   */
  public void setRotateEnabled(boolean enabled) {
    rotateButton.setDisable(!enabled);
  }

  // Getters for the buttons in case you need to attach ActionListeners directly
  public Button getImageButton() {
    return imageButton;
  }

  public Button getCropButton() {
    return cropButton;
  }

  public Button getInvertButton() {
    return invertButton;
  }

  public Button getRotateButton() {
    return rotateButton;
  }
}

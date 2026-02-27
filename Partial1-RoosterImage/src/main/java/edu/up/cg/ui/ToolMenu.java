package edu.up.cg;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
/*
 * ToolMenu
 * - this is the toolbar at the top of the editor, it holds all the action buttons
 * - extends ToolBar so it plugs directly into the VBox layout in App without extra wrapping
 * - buttons are icon-only, built from public domain SVG paths so no license is needed
 *
 * - it owns no logic, only exposes getters for each button
 *   - all event binding happens individually with the buttons, keeping ui and behavior separated
 *   - rotateDegrees is the control with state, read by App before applying the rotation
 *
 *
 *  - todo: should be extendable, not hardcoded
 * */
public class ToolMenu extends ToolBar {

  private final Button imageButton;
  private final Button cropButton;
  private final Button invertButton;
  private final Button rotateButton;
  private final ComboBox<Integer> rotateDegrees;
  private final Button saveButton;

  public ToolMenu() {
    // this are copied from public domain icons (so no license needed)
    final String imageSvg = "m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 0 0 1.5-1.5V6a1.5 1.5 0 0 0-1.5-1.5H3.75A1.5 1.5 0 0 0 2.25 6v12a1.5 1.5 0 0 0 1.5 1.5Zm10.5-11.25h.008v.008h-.008V8.25Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Z";
    final String cropSvg = "M7,17V1H5v4H1v2h4v10c0,1.1,0.9,2,2,2h10v4h2v-4h4v-2H7z";
    final String invertSvg = "M12,2C6.48,2,2,6.48,2,12s4.48,10,10,10s10-4.48,10-10S17.52,2,12,2z M12,20V4c4.41,0,8,3.59,8,8S16.41,20,12,20z";
    final String rotateSvg = "M12,5V1L7,6l5,5V7c3.31,0,6,2.69,6,6s-2.69,6-6,6s-6-2.69-6-6H4c0,4.42,3.58,8,8,8s8-3.58,8-8S16.42,5,12,5z";
    final String saveSvg = "M17,3H5A2,2 0 0,0 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V7L17,3M12,19A2,2 0 0,1 10,17A2,2 0 0,1 12,15A2,2 0 0,1 14,17A2,2 0 0,1 12,19M6,5H15V9H6V5Z";

    // Initialize buttons
    imageButton = createIconButton(imageSvg, "load image");
    cropButton = createIconButton(cropSvg, "crop to region");
    invertButton = createIconButton(invertSvg, "invert colors");
    rotateButton = createIconButton(rotateSvg, "rotate region");
    saveButton = createIconButton(saveSvg, "save image");
    rotateDegrees = new ComboBox<>(
        FXCollections.observableArrayList(90, 180, 270));
    rotateDegrees.setValue(90);
    rotateDegrees.setTooltip(new Tooltip("rotation degrees"));

    // Add buttons to the ToolBar
    this.getItems().addAll(
        imageButton,
        cropButton,
        invertButton,
        rotateButton,
        rotateDegrees,
        saveButton);
  }

  public int getRotateDegrees() {
    return rotateDegrees.getValue();
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


  // only getter as only the actions are bound from outside the class
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

  public Button getSaveButton() {
    return saveButton;
  }
}

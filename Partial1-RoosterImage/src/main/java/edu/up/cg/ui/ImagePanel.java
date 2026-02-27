package edu.up.cg;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

/*
 * ImagePanel
 * - this is the visual editor area, it shows the image and handles region selection
 * - extends StackPane so the selection rectangle is on top, not below like in a vbox 
 *
 * - it owns two layers:
 *   - ImageView: renders the loaded image, preserving aspect ratio and fitting the panel
 *   - Rectangle (selection): a semi-transparent yellow overlay drawn by mouse drag
 *
 * - selection works in two coordinate spaces:
 *   - screen space: where the rectangle is drawn on the panel 
 *   - pixel space: the actual image coordinates (pixelX, pixelY, pixelW, pixelH)
 *     - these are what gets passed to modifiers via getRegion()
 *     - the conversion is done on mouse release using the scale between display size and image size
 *
 * - if the user clicks without dragging, it defaults to selecting the whole image
 *   - this way every modifier always has a valid region to work on, no null checks needed outside
 * */
public class ImagePanel extends StackPane {
  private Rectangle selection = new Rectangle();
  private final ImageView imageview;
  private Image image;

  private double dragStartX;
  private double dragStartY;

  // pixel-space selection
  private int pixelX;
  private int pixelY;
  private int pixelW;
  private int pixelH;

  ImagePanel() {

    this.imageview = new ImageView();
    this.imageview.setPreserveRatio(true);

    this.selection = new Rectangle(0, 0, 0, 0);
    this.selection.setFill(Color.rgb(255, 255, 0, .5));
    this.selection.setStroke(Color.YELLOW);

    this.getChildren().add(this.imageview);
    this.getChildren().add(selection);

    StackPane.setAlignment(imageview, Pos.CENTER);
    this.selection.setManaged(false);
    registerSelectionHandlers();
  }

  public void setImage(final Image newone) {
    this.image = newone;
    this.imageview.setImage(this.image);
    this.imageview.setFitWidth(this.getWidth() * .9);
    this.imageview.setFitHeight(this.getHeight() * .9);
    selectWholeImage();
  }

  public boolean hasImage() {
    return this.image != null && !this.image.isError();
  }

  public Rectangle2D getRegion() {
    return new Rectangle2D(
        pixelX,
        pixelY,
        pixelW,
        pixelH);
  }

  public Image getImage() {
    return this.image;
  }

  private void selectWholeImage() {
    if (!hasImage())
      return;

    final Bounds ib = imageview.getBoundsInParent();

    // pixel space
    pixelX = 0;
    pixelY = 0;
    pixelW = (int) image.getWidth();
    pixelH = (int) image.getHeight();

    // selection rectangle
    selection.setX(ib.getMinX());
    selection.setY(ib.getMinY());
    selection.setWidth(ib.getWidth());
    selection.setHeight(ib.getHeight());
    selection.setVisible(false);

  }

  // Math.clamp didnt wanted to work
  private double clamp(final double value, final double min, final double max) {
    return Math.max(min, Math.min(max, value));
  }

  private void registerSelectionHandlers() {

    selection.setManaged(false);
    selection.setVisible(false);

    this.setOnMousePressed(e -> {
      if (!hasImage())
        return;

      final Bounds ib = imageview.getBoundsInParent();

      // clamp start point inside image
      dragStartX = clamp(e.getX(), ib.getMinX(), ib.getMaxX());
      dragStartY = clamp(e.getY(), ib.getMinY(), ib.getMaxY());

      selection.setX(dragStartX);
      selection.setY(dragStartY);
      selection.setWidth(0);
      selection.setHeight(0);
      selection.setVisible(true);
    });

    this.setOnMouseDragged(e -> {
      if (!hasImage())
        return;

      final Bounds ib = imageview.getBoundsInParent();

      // clamp current mouse position to integers
      final double currentX = clamp(e.getX(), ib.getMinX(), ib.getMaxX());
      final double currentY = clamp(e.getY(), ib.getMinY(), ib.getMaxY());

      final double x = Math.min(dragStartX, currentX);
      final double y = Math.min(dragStartY, currentY);
      final double w = Math.abs(currentX - dragStartX);
      final double h = Math.abs(currentY - dragStartY);

      selection.setX(x);
      selection.setY(y);
      selection.setWidth(w);
      selection.setHeight(h);
    });

    this.setOnMouseReleased(e -> {
      if (!hasImage())
        return;

      final Bounds ib = imageview.getBoundsInParent();

      final double w = selection.getWidth();
      final double h = selection.getHeight();

      // If user did not drag → select whole image
      if (w < 1 || h < 1) {
        selectWholeImage();
        return;
      }
      this.selection.setVisible(true);

      // Convert to pixel coordinates
      final double scaleX = image.getWidth() / ib.getWidth();
      final double scaleY = image.getHeight() / ib.getHeight();

      pixelX = (int) ((selection.getX() - ib.getMinX()) * scaleX);
      pixelY = (int) ((selection.getY() - ib.getMinY()) * scaleY);
      pixelW = (int) (w * scaleX);
      pixelH = (int) (h * scaleY);

    });
  }

}

package edu.up.cg;

import java.io.File;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class ImagePanel extends StackPane {
  private final Rectangle selection;
  private final ImageView imageview;
  private final Image image;

  // Store the selection in the original image's actual pixel coordinates
  private double pixelX;
  private double pixelY;
  private double pixelW;
  private double pixelH;

  ImagePanel(final String fileToEdit) {

    this.image = new Image(fileToEdit);
    if (this.image.isError()) {
      throw new RuntimeException("unable to load file " + fileToEdit);
    }
    
    this.imageview = new ImageView(this.image);
    this.imageview.setPreserveRatio(true);
    
    this.selection = new Rectangle();
    this.selection.setFill(Color.rgb(0, 150, 255, 0.3));  
    this.selection.setStroke(Color.BLUE);

    StackPane.setAlignment(imageview, Pos.CENTER);
    StackPane.setAlignment(selection, Pos.TOP_LEFT);

    this.getChildren().addAll(imageview, selection);

    this.heightProperty().addListener((property, oldValue, newValue) -> {
      this.imageview.setFitHeight(newValue.doubleValue());
      updateSelectionScale();
    });
    
    this.widthProperty().addListener((property, oldValue, newValue) -> {
      this.imageview.setFitWidth(newValue.doubleValue());
      updateSelectionScale();
    });

    selectWholeImage();
  }

  private void selectWholeImage() {
    this.pixelX = 0;
    this.pixelY = 0;
    this.pixelW = this.image.getWidth();
    this.pixelH = this.image.getHeight();
    
    updateSelectionScale();
  }

  /**
   * Scales and positions the visual selection rectangle to  match 
   * the visually scaled image within the ImageView.
   */
  private void updateSelectionScale() {
    if (image.getWidth() == 0 || image.getHeight() == 0 || getWidth() == 0 || getHeight() == 0) return;

    // Calculate the actual scale factor applied by the ImageView (preserveRatio = true)
    final double scale = Math.min(
        imageview.getFitWidth() / image.getWidth(),
        imageview.getFitHeight() / image.getHeight()
    );

    // Calculate the actual displayed width and height of the image on the screen
    final double displayedWidth = image.getWidth() * scale;
    final double displayedHeight = image.getHeight() * scale;

    // Because StackPane centers the ImageView, calculate the visual X and Y empty space offsets
    final double offsetX = (this.getWidth() - displayedWidth) / 2.0;
    final double offsetY = (this.getHeight() - displayedHeight) / 2.0;

    // Scale the visual rectangle
    this.selection.setWidth(this.pixelW * scale);
    this.selection.setHeight(this.pixelH * scale);

    // Position the visual rectangle, factoring in the StackPane offset
    this.selection.setTranslateX(offsetX + (this.pixelX * scale));
    this.selection.setTranslateY(offsetY + (this.pixelY * scale));
  }

  /**
   * Returns a Rectangle containing the original, unscaled pixel coordinates of the selection.
   * (Initial Pixel X, Initial Pixel Y, Original Width, Original Height)
   */
  public Rectangle getOriginalPixelSelection() {
    return new Rectangle(this.pixelX, this.pixelY, this.pixelW, this.pixelH);
  }
}

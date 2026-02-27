package edu.up.cg.modifiers;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * General ImageModifier that is implemented by child classes
 *
 * just a transformation to given image
 * can virtually do whatever we want
 * all the region is given if no selection was made
 */
public interface ImageModifier {

  public Image apply(Image image, Rectangle2D region);

}

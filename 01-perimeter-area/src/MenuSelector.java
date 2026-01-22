import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class MenuSelector extends MenuBar {
    private final StringProperty selectedShape = new SimpleStringProperty();

    public MenuSelector() {
        Menu shapesMenu = new Menu("Shapes");

        shapesMenu.getItems().addAll(
                makeEntry("Square"),
                makeEntry("Rectangle"),
                makeEntry("Triangle"),
                makeEntry("Circle"),
                makeEntry("Pentagon"),
                makeEntry("Semicircle")
        );

        getMenus().add(shapesMenu);
    }

    private MenuItem makeEntry(String name) {
        MenuItem item = new MenuItem(name);
        item.setOnAction(e -> selectedShape.set(name));
        return item;
    }

    public ImageView figurePreview() {
        if (selectedShape.get() == null) return null;

        String path = "/figures/" + selectedShape.get() + ".png";
        Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));

        ImageView view = new ImageView(img);
        view.setPreserveRatio(true);
        view.setFitWidth(150);

        return view;
    }

    public StringProperty selectedShapeProperty() {
        return selectedShape;
    }
}

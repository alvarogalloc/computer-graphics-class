import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class GeometryApplication extends Application {
    private InputProvider inputs ;
    private String currentShape;
    private Label areaValue;
    private Label perimeterValue;

    @Override
    public void start(Stage stage) {
        inputs = new InputProvider(
                this::updateCalculation
        );
        Pane root = makeLayout();

        Scene scene = new Scene(root, 400, 500);

        stage.setTitle("01 - Area & perimeter calculator");
        stage.setScene(scene);
        stage.show();

    }


    private void updateCalculation(Map<String, Double > inputs) {
        //System.out.println(inputs.toString());
        double area = AreaCalculator.calculate(this.currentShape, inputs);
        double perimeter = PerimeterCalculator.calculate(this.currentShape, inputs);
        //System.out.println("the perimeter " + this.currentShape + "is: " +perimeter);
        //System.out.println("the area of " + this.currentShape + " is: " +area);
        areaValue.setText(String.format("%.3f", area));
        perimeterValue.setText(String.format("%.3f", perimeter));

    }

    private Pane makeLayout() {

        MenuSelector menu = new MenuSelector();
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        root.setTop(menu);

        menu.selectedShapeProperty().addListener((obs, oldV, newV) -> {
            ImageView preview = menu.figurePreview();
            root.setCenter(preview);
        });

        // default just the text of choose somethin
        Label initText = new Label("Choose a figure from the menu");
        root.setCenter(initText);

        menu.selectedShapeProperty().addListener((obs, oldShape, newShape) -> {
            inputs.makeInputs(newShape);
            this.currentShape = newShape;
        });


        // now we need the output, hardcoded ik but its not gonna grow anyway
        Label areaLabel = new Label("Area:");
        areaValue = new Label("—");
        HBox areaGroup = new HBox(5, areaLabel, areaValue);

        Label perimeterLabel = new Label("Perimeter:");
        perimeterValue = new Label("—");
        HBox perimeterGroup = new HBox(5, perimeterLabel, perimeterValue);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox resultsRow = new HBox(10, areaGroup, spacer, perimeterGroup);
        resultsRow.setPadding(new Insets(10, 0, 0, 0));

        VBox bottomPanel = new VBox(12);
        bottomPanel.getChildren().addAll(inputs, resultsRow);
        root.setBottom(bottomPanel);

        return root;
    }

    public static void run(String[] args) {
        launch(args);
    }
}


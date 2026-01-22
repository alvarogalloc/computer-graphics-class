import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class InputProvider extends VBox {

    static class DoubleField {

        public static TextFormatter<Double> create() {
            UnaryOperator<TextFormatter.Change> filter = change -> {
                String text = change.getControlNewText();

                if (text.matches("-?\\d*(\\.\\d*)?")) {
                    return change;
                }
                return null;
            };

            return new TextFormatter<>(filter);
        }
    }

    public record InputSpec(String key, String label) {

    }

    public static final Map<String, List<InputSpec>> INPUTS = Map.of(
            "Square", List.of(
                    new InputSpec("side", "Side")
            ),

            "Rectangle", List.of(
                    new InputSpec("width", "Width"),
                    new InputSpec("height", "Height")
            ),

            "Triangle", List.of(
                    new InputSpec("base", "Base"),
                    new InputSpec("height", "Height"),
                    new InputSpec("sideA", "Side A"),
                    new InputSpec("sideB", "Side B"),
                    new InputSpec("sideC", "Side C")
            ),

            "Circle", List.of(
                    new InputSpec("radius", "Radius")
            ),

            "Pentagon", List.of(
                    new InputSpec("side", "Side"),
                    new InputSpec("apothem", "Apothem")
            ),

            "Semicircle", List.of(
                    new InputSpec("radius", "Radius")
            )
    );

    private Map<String, Double> inputValues;
    final private Consumer<Map<String, Double>> onChangeCallback;


    public InputProvider(Consumer<Map<String, Double>> onChangeCallback) {
        setPadding(new Insets(3));
        inputValues = new HashMap<>();
        this.onChangeCallback = onChangeCallback;
    }

    public void makeInputs(String shape) {
        getChildren().clear();

        var specs = INPUTS.get(shape);
        if (specs == null) return;
        if (inputValues != null) {
            inputValues.clear();
        }

        Label inputsLabel = new Label("Enter the inputs:");
        inputsLabel.setStyle("-fx-font-weight: bold");

        getChildren().add(inputsLabel);

        for (InputSpec spec : specs) {
            Label label = new Label(spec.label());
            TextField field = new TextField();
            field.textProperty().setValue("0");
            field.setPromptText(spec.label());
            field.setId(spec.key());
            field.setTextFormatter(DoubleField.create()); // only accept floating points
            field.textProperty().addListener((obs, oldV, newV) -> {
                if (!newV.isBlank()) {
                    double newVal = Double.parseDouble(newV);
                    if (newVal == 0.0) {
                        inputValues.remove(spec.key());
                    } else {

                        inputValues.put(spec.key(), newVal);
                    }

                }
                if (onChangeCallback != null) {
                    onChangeCallback.accept(inputValues);
                }

            });
            getChildren().addAll(label, field);
        }
    }

}

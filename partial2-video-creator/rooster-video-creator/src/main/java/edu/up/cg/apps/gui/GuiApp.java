package edu.up.cg.apps.gui;

import edu.up.cg.integrations.ai.AIService;
import edu.up.cg.integrations.common.CommandRunner;
import edu.up.cg.integrations.exiftool.ExifToolCliService;
import edu.up.cg.integrations.ffmpeg.FFmpegCliService;
import edu.up.cg.integrations.ffmpeg.FFmpegService;
import edu.up.cg.integrations.gemini.GeminiApiService;
import edu.up.cg.integrations.map.MapService;
import edu.up.cg.integrations.map.MapboxOsmService;
import edu.up.cg.integrations.metadata.MetadataService;
import edu.up.cg.pipeline.PipelineContext;
import edu.up.cg.pipeline.PipelineExecutionReport;
import edu.up.cg.pipeline.PipelineProgressListener;
import edu.up.cg.pipeline.PipelineStep;
import edu.up.cg.pipeline.StepExecutionResult;
import edu.up.cg.pipeline.VideoPipelineOrchestrator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GuiApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private final ObservableList<Path> mediaModel = FXCollections.observableArrayList();
    private final TextArea contextPreviewArea = new TextArea();
    private final TextArea stepsArea = new TextArea();
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label outputDirectoryLabel = new Label();
    private Path outputDirectory = Path.of("output-gui");
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.setTitle("Video Creator GUI - JavaFX");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Top Panel
        HBox topPanel = new HBox(10);
        topPanel.setAlignment(Pos.CENTER_LEFT);
        Button addFilesButton = new Button("Add Media");
        Button clearFilesButton = new Button("Clear");
        Button pickOutputButton = new Button("Output Folder");
        Button runButton = new Button("Run Pipeline");

        topPanel.getChildren().addAll(addFilesButton, clearFilesButton, pickOutputButton, runButton);

        outputDirectoryLabel.setText("Output: " + outputDirectory.toAbsolutePath());
        outputDirectoryLabel.setStyle("-fx-font-weight: bold;");

        // Media List
        ListView<Path> mediaList = new ListView<>(mediaModel);
        mediaList.setPrefWidth(300);

        contextPreviewArea.setEditable(false);
        stepsArea.setEditable(false);

        VBox rightSplit = new VBox(10, new Label("Context Preview"), contextPreviewArea, new Label("Execution Logs"), stepsArea);
        VBox.setVgrow(contextPreviewArea, Priority.ALWAYS);
        VBox.setVgrow(stepsArea, Priority.ALWAYS);
        rightSplit.setPrefWidth(700);

        HBox mainSplit = new HBox(15, mediaList, rightSplit);
        HBox.setHgrow(rightSplit, Priority.ALWAYS);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        // Progress
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(25);

        root.getChildren().addAll(topPanel, mainSplit, outputDirectoryLabel, progressBar);

        addFilesButton.setOnAction(e -> addMediaFiles());
        clearFilesButton.setOnAction(e -> {
            mediaModel.clear();
            refreshContextPreview();
        });
        pickOutputButton.setOnAction(e -> pickOutputDirectory());
        runButton.setOnAction(e -> runPipeline(runButton));

        refreshContextPreview();

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addMediaFiles() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Media files", "*.jpg", "*.jpeg", "*.png", "*.mp4", "*.mov", "*.mkv"));
        
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files != null) {
            for (File file : files) {
                mediaModel.add(file.toPath());
            }
            refreshContextPreview();
        }
    }

    private void pickOutputDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            outputDirectory = dir.toPath();
            outputDirectoryLabel.setText("Output: " + outputDirectory.toAbsolutePath());
            refreshContextPreview();
        }
    }

    private void refreshContextPreview() {
        StringBuilder preview = new StringBuilder();
        preview.append("Context preview\n");
        preview.append("Output directory: ").append(outputDirectory.toAbsolutePath()).append("\n");
        preview.append("Media count: ").append(mediaModel.size()).append("\n");
        for (int i = 0; i < mediaModel.size(); i++) {
            preview.append(i + 1).append(". ").append(mediaModel.get(i).toAbsolutePath()).append("\n");
        }
        contextPreviewArea.setText(preview.toString());
    }

    private void runPipeline(Button runButton) {
        if (mediaModel.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Add at least one media file before running.");
            alert.showAndWait();
            return;
        }

        List<Path> media = new ArrayList<>(mediaModel);

        MetadataService metadataService = new ExifToolCliService();
        AIService aiService = new GeminiApiService();
        FFmpegService ffmpegService = new FFmpegCliService();
        MapService mapService = new MapboxOsmService();
        CommandRunner commandRunner = new CommandRunner();

        VideoPipelineOrchestrator orchestrator = new VideoPipelineOrchestrator(
            metadataService, aiService, ffmpegService, mapService, commandRunner
        );

        runButton.setDisable(true);
        stepsArea.setText("");
        progressBar.setProgress(0);

        Task<PipelineExecutionReport> task = new Task<>() {
            @Override
            protected PipelineExecutionReport call() {
                return orchestrator.runAllConfiguredSteps(media, outputDirectory, new PipelineProgressListener() {
                    private int totalStepsLoc = 1;

                    @Override
                    public void onPipelineStarted(PipelineContext context, int totalStepsParam) {
                        this.totalStepsLoc = Math.max(totalStepsParam, 1);
                        updateMessage("Pipeline started with " + context.getSourceMedia().size() + " media files\n");
                    }

                    @Override
                    public void onStepStarted(PipelineStep step, int index, int totalSteps) {
                        double percent = ((index - 1) / (double) totalSteps);
                        updateProgress(percent, 1.0);
                        updateMessage("Running " + step.getStepName() + " (" + index + "/" + totalSteps + ")\n");
                    }

                    @Override
                    public void onStepFinished(StepExecutionResult result, int index, int totalSteps) {
                        double percent = (index / (double) totalSteps);
                        updateProgress(percent, 1.0);
                        updateMessage(result.getStepName() + " -> " + (result.isCompleted() ? "COMPLETED" : "FAILED") + "\n" +
                                      "  " + result.getDetails() + "\n");
                    }

                    @Override
                    public void onPipelineFinished(PipelineExecutionReport report) {
                        if (report.isSuccessful()) {
                            updateProgress(1.0, 1.0);
                            updateMessage("Pipeline finished. Success: true\n");
                        } else {
                            int executed = report.getStepResults().size();
                            double percent = (executed / (double) Math.max(this.totalStepsLoc, 1));
                            updateProgress(percent, 1.0);
                            updateMessage("Pipeline failed and stopped early. Success: false\n");
                        }
                    }
                });
            }
        };

        task.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            if (newMsg != null) {
                stepsArea.appendText(newMsg);
            }
        });
        
        task.progressProperty().addListener((obs, oldProgress, newProgress) -> progressBar.setProgress(newProgress.doubleValue()));

        task.setOnSucceeded(e -> {
            runButton.setDisable(false);
            try {
                PipelineExecutionReport report = task.getValue();
                progressBar.setProgress(report.isSuccessful() ? 1.0 : progressBar.getProgress());
            } catch (Exception ex) {
                stepsArea.appendText("Pipeline crashed: " + ex.getMessage() + "\n");
                Alert alert = new Alert(Alert.AlertType.ERROR, "Pipeline failed: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        task.setOnFailed(e -> {
            runButton.setDisable(false);
            Throwable ex = task.getException();
            stepsArea.appendText("Pipeline crashed: " + ex.getMessage() + "\n");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Pipeline failed: " + ex.getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }
}

package edu.up.cg.apps.cli;

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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CliApp {

    public static void main(String[] args) {
        CliArguments parsed = parseArguments(args);
        if (parsed.mediaFiles.isEmpty()) {
            printUsage();
            return;
        }

        MetadataService metadataService = new ExifToolCliService();
        AIService aiService = new GeminiApiService();
        FFmpegService ffmpegService = new FFmpegCliService();
        MapService mapService = new MapboxOsmService();
        CommandRunner commandRunner = new CommandRunner();

        VideoPipelineOrchestrator orchestrator = new VideoPipelineOrchestrator(
            metadataService,
            aiService,
            ffmpegService,
            mapService,
            commandRunner
        );

        PipelineExecutionReport report = orchestrator.runAllConfiguredSteps(
            parsed.mediaFiles,
            parsed.outputDirectory,
            new ConsoleProgressListener()
        );

        System.out.println();
        System.out.println("Pipeline finished. Success: " + report.isSuccessful());
        for (StepExecutionResult result : report.getStepResults()) {
            System.out.println(result.getStepName() + " -> " + (result.isCompleted() ? "COMPLETED" : "FAILED"));
            System.out.println("  " + result.getDetails());
        }

        if (!report.isSuccessful()) {
            System.exit(1);
        }
    }

    private static CliArguments parseArguments(String[] args) {
        Path outputDirectory = Path.of("output");
        List<Path> mediaFiles = new ArrayList<>();

        List<String> all = Arrays.asList(args);
        for (int i = 0; i < all.size(); i++) {
            String token = all.get(i);
            if ("--out".equals(token) && i + 1 < all.size()) {
                outputDirectory = Path.of(all.get(i + 1));
                i++;
            } else {
                mediaFiles.add(Path.of(token));
            }
        }

        return new CliArguments(outputDirectory, mediaFiles);
    }

    private static void printUsage() {
        System.out.println("Usage: java ... CliApp [--out output_dir] <media1> <media2> ...");
        System.out.println("Example: java ... CliApp --out demo-output samples/a.jpg samples/b.mp4");
    }

    private static final class CliArguments {
        private final Path outputDirectory;
        private final List<Path> mediaFiles;

        private CliArguments(Path outputDirectory, List<Path> mediaFiles) {
            this.outputDirectory = outputDirectory;
            this.mediaFiles = List.copyOf(mediaFiles);
        }
    }

    private static final class ConsoleProgressListener implements PipelineProgressListener {
        private int totalSteps;

        @Override
        public void onPipelineStarted(PipelineContext context, int totalSteps) {
            this.totalSteps = Math.max(totalSteps, 1);
            System.out.println("Starting pipeline with " + context.getSourceMedia().size() + " media files");
            System.out.println("Output directory: " + context.getOutputDirectory().toAbsolutePath());
            System.out.println("Total steps: " + this.totalSteps);
        }

        @Override
        public void onStepStarted(PipelineStep step, int index, int totalSteps) {
            renderProgress(index - 1, totalSteps, "Running " + step.getStepName());
        }

        @Override
        public void onStepFinished(StepExecutionResult result, int index, int totalSteps) {
            renderProgress(index, totalSteps, result.getStepName() + " -> " + (result.isCompleted() ? "OK" : "FAIL"));
            System.out.println();
            System.out.println("  " + result.getDetails());
        }

        @Override
        public void onPipelineFinished(PipelineExecutionReport report) {
            int executed = report.getStepResults().size();
            if (report.isSuccessful()) {
                renderProgress(totalSteps, totalSteps, "Pipeline complete");
            } else {
                renderProgress(executed, totalSteps, "Pipeline failed (stopped early)");
            }
            System.out.println();
        }

        private void renderProgress(int done, int total, String label) {
            int safeTotal = Math.max(total, 1);
            int width = 32;
            int filled = (int) ((done / (double) safeTotal) * width);
            String bar = "#".repeat(Math.max(0, filled)) + "-".repeat(Math.max(0, width - filled));
            int percent = (int) ((done / (double) safeTotal) * 100.0);
            System.out.print("\r[" + bar + "] " + String.format("%3d", percent) + "% " + label);
        }
    }
}

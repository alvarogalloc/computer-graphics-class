package edu.up.cg.pipeline;

import edu.up.cg.integrations.ai.AIService;
import edu.up.cg.integrations.common.CommandRunner;
import edu.up.cg.integrations.ffmpeg.FFmpegService;
import edu.up.cg.integrations.map.MapService;
import edu.up.cg.integrations.metadata.MetadataService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideoPipelineOrchestrator {
    private final List<PipelineStep> steps;
    private final MetadataService metadataService;
    private final AIService aiService;
    private final FFmpegService ffmpegService;
    private final MapService mapService;
    private final CommandRunner commandRunner;

    public VideoPipelineOrchestrator(
        MetadataService metadataService,
        AIService aiService,
        FFmpegService ffmpegService,
        MapService mapService,
        CommandRunner commandRunner
    ) {
        this.metadataService = Objects.requireNonNull(metadataService, "metadataService");
        this.aiService = Objects.requireNonNull(aiService, "aiService");
        this.ffmpegService = Objects.requireNonNull(ffmpegService, "ffmpegService");
        this.mapService = Objects.requireNonNull(mapService, "mapService");
        this.commandRunner = Objects.requireNonNull(commandRunner, "commandRunner");

        this.steps = List.of(
            new Step01EssenceImage(),
            new Step02NarrationTts(),
            new Step03VisualTimeline(),
            new Step04FinalMerge(),
            new Step05MapOutro()
        );
    }

    public List<PipelineStep> getOrderedSteps() {
        return steps;
    }

    public PipelineExecutionReport runFirstStepOnly(List<Path> sourceMedia, Path outputDirectory, PipelineProgressListener listener) {
        PipelineContext context = buildContext(sourceMedia, outputDirectory);
        PipelineProgressListener safeListener = listener == null ? PipelineProgressListener.noOp() : listener;

        safeListener.onPipelineStarted(context, 1);
        safeListener.onStepStarted(steps.get(0), 1, 1);
        StepExecutionResult firstStep = executeStepSafely(steps.get(0), context);
        safeListener.onStepFinished(firstStep, 1, 1);

        PipelineExecutionReport report = new PipelineExecutionReport(List.of(firstStep));
        safeListener.onPipelineFinished(report);
        return report;
    }

    public PipelineExecutionReport runAllConfiguredSteps(List<Path> sourceMedia, Path outputDirectory, PipelineProgressListener listener) {
        PipelineContext context = buildContext(sourceMedia, outputDirectory);
        PipelineProgressListener safeListener = listener == null ? PipelineProgressListener.noOp() : listener;

        List<StepExecutionResult> results = new ArrayList<>();
        safeListener.onPipelineStarted(context, steps.size());

        for (int i = 0; i < steps.size(); i++) {
            PipelineStep step = steps.get(i);
            safeListener.onStepStarted(step, i + 1, steps.size());
            StepExecutionResult result = executeStepSafely(step, context);
            results.add(result);

            safeListener.onStepFinished(result, i + 1, steps.size());
            if (!result.isCompleted()) {
                break;
            }
        }

        PipelineExecutionReport report = new PipelineExecutionReport(results);
        safeListener.onPipelineFinished(report);
        return report;
    }

    private PipelineContext buildContext(List<Path> sourceMedia, Path outputDirectory) {
        Objects.requireNonNull(sourceMedia, "sourceMedia");
        Objects.requireNonNull(outputDirectory, "outputDirectory");

        try {
            Files.createDirectories(outputDirectory);
        } catch (Exception e) {
            throw new IllegalStateException("Could not create output directory: " + outputDirectory, e);
        }

        return new PipelineContext(
            sourceMedia,
            outputDirectory,
            metadataService,
            aiService,
            ffmpegService,
            mapService,
            commandRunner,
            new PipelineRuntimeState()
        );
    }

    private StepExecutionResult executeStepSafely(PipelineStep step, PipelineContext context) {
        try {
            return step.execute(context);
        } catch (Throwable e) {
            return new StepExecutionResult(step.getStage(), step.getStepName(), false, e.getMessage());
        }
    }
}

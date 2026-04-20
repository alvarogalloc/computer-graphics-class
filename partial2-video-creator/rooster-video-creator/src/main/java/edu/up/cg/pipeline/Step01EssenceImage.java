package edu.up.cg.pipeline;

import edu.up.cg.integrations.metadata.MediaMetadata;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Step 01: Essence Image
 * 
 * Receives: A verified pipeline context containing ordered media metadata.
 * Does: 
 *   - Extracts the geographical location and details from the first image in the timeline.
 *   - Requests the AI service to generate a short, evocative descriptive prompt characterizing the first detected location.
 * Outputs: The generated essence prompt text (assigned to state and written to `01_essence_prompt.txt`).
 */
public class Step01EssenceImage implements PipelineStep {

    @Override
    public String getStepName() {
        return "Step01EssenceImage";
    }

    @Override
    public PipelineStage getStage() {
        return PipelineStage.ESSENCE_IMAGE;
    }

    @Override
    public StepExecutionResult execute(PipelineContext context) {
        try {
            List<MediaMetadata> orderedMetadata = context.getSourceMedia().stream()
                .map(path -> context.getMetadataService().readMetadata(path))
                .sorted(Comparator.comparing(this::sortableCapturedAt))
                .collect(Collectors.toList());

            context.getState().getOrderedMediaMetadata().clear();
            context.getState().getOrderedMediaMetadata().addAll(orderedMetadata);

            String mediaSummary = buildMediaSummary(orderedMetadata);
            String generatedPrompt = context.getAiService().generateEssenceImagePrompt(mediaSummary);
            context.getState().setEssencePrompt(generatedPrompt);

            Path promptFile = context.getOutputDirectory().resolve("01_essence_prompt.txt");
            Files.writeString(promptFile, generatedPrompt, StandardCharsets.UTF_8);
            context.getState().setEssencePromptFile(promptFile);

            String details = "Generated essence prompt and wrote file: " + promptFile;
            return new StepExecutionResult(getStage(), getStepName(), true, details);
        } catch (Exception e) {
            return new StepExecutionResult(getStage(), getStepName(), false, "Step failed: " + e.getMessage());
        }
    }

    private String buildMediaSummary(List<MediaMetadata> orderedMetadata) {
        StringBuilder summary = new StringBuilder();

        summary.append("Step 1 ordered media count: ").append(orderedMetadata.size()).append(System.lineSeparator());
        for (MediaMetadata metadata : orderedMetadata) {
            summary.append("- file: ").append(metadata.getSourcePath().toAbsolutePath()).append(System.lineSeparator());
            summary.append("  type: ").append(metadata.getMediaType()).append(System.lineSeparator());
            summary.append("  capturedAt: ").append(metadata.getCapturedAt().map(Object::toString).orElse("unknown")).append(System.lineSeparator());
            summary.append("  location: ").append(formatLocation(metadata)).append(System.lineSeparator());
        }

        return summary.toString();
    }

    private String formatLocation(MediaMetadata metadata) {
        return metadata.getLocation()
            .map(location -> location.getLatitude() + "," + location.getLongitude())
            .orElse("unknown");
    }

    private LocalDateTime sortableCapturedAt(MediaMetadata metadata) {
        return metadata.getCapturedAt().orElse(LocalDateTime.MAX);
    }
}
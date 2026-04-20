package edu.up.cg.pipeline;

import edu.up.cg.integrations.ffmpeg.PortraitResolution;
import edu.up.cg.integrations.metadata.MediaMetadata;
import edu.up.cg.integrations.metadata.MediaType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Step 03: Visual Timeline Creation
 * 
 * Receives: 
 *   - The list of media files.
 *   - The list of individually generated AI audio tracks from Step 02 (`narrations`).
 * Does: 
 *   - Converts the source media to portrait format via FFmpeg (scaled & padded).
 *   - Loops static images to fit precisely against the length of their respective generated narration track (handling `-shortest`).
 *   - Merges all individual clips chronologically into one unified AV stream.
 * Outputs: The `03_timeline.mp4` video containing aligned visuals & audio combined.
 */
public class Step03VisualTimeline implements PipelineStep {

    @Override
    public String getStepName() {
        return "Step03VisualTimeline";
    }

    @Override
    public PipelineStage getStage() {
        return PipelineStage.VISUAL_TIMELINE_VIDEO;
    }

    @Override
    public StepExecutionResult execute(PipelineContext context) {
        try {
            List<MediaMetadata> orderedMetadata = context.getState().getOrderedMediaMetadata();
            if (orderedMetadata.isEmpty()) {
                return new StepExecutionResult(getStage(), getStepName(), false, "Step 01 must run first: no ordered metadata found");
            }

            List<Path> narrationAudios = context.getState().getIndividualNarrationAudios();
            if (narrationAudios == null || narrationAudios.isEmpty()) {
                return new StepExecutionResult(getStage(), getStepName(), false, "Step 02 must run first: individual narrations missing");
            }

            Path clipsDirectory = context.getOutputDirectory().resolve("03_clips");
            Files.createDirectories(clipsDirectory);

            List<Path> clipFiles = new ArrayList<>();
            for (int i = 0; i < orderedMetadata.size(); i++) {
                MediaMetadata metadata = orderedMetadata.get(i);
                Path clipFile = clipsDirectory.resolve(String.format("clip_%03d.mp4", i + 1));
                Path audioTask = (i < narrationAudios.size()) ? narrationAudios.get(i) : null;

                if (metadata.getMediaType() == MediaType.IMAGE) {
                    if (audioTask != null && Files.exists(audioTask)) {
                        context.getFfmpegService().createVideoFromImageAndAudio(
                            metadata.getSourcePath(),
                            audioTask,
                            clipFile,
                            PortraitResolution.HD_1080x1920
                        );
                    } else {
                        context.getFfmpegService().createStillImageVideo(
                            metadata.getSourcePath(),
                            clipFile,
                            PortraitResolution.HD_1080x1920,
                            3,
                            true
                        );
                    }
                } else {
                    // For video files, we'd normally just convert to portrait.
                    // But if we want narration we should mix it. Right now we don't have a specific requirement to overwrite video audio, so just scale it.
                    context.getFfmpegService().convertToPortraitCover(
                        metadata.getSourcePath(),
                        clipFile,
                        PortraitResolution.HD_1080x1920
                    );
                }

                clipFiles.add(clipFile);
            }

            Path timelineVideo = context.getOutputDirectory().resolve("03_timeline.mp4");
            context.getFfmpegService().concatWithAudio(clipFiles, timelineVideo);
            context.getState().setTimelineVideoFile(timelineVideo);

            return new StepExecutionResult(getStage(), getStepName(), true, "Visual timeline bounded to audio generated: " + timelineVideo);
        } catch (Exception e) {
            return new StepExecutionResult(getStage(), getStepName(), false, "Step failed: " + e.getMessage());
        }
    }
}

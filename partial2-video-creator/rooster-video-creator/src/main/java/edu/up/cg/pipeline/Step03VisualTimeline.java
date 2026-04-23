package edu.up.cg.pipeline;

import edu.up.cg.integrations.ffmpeg.FFmpegCliService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Step 03: Visual Timeline
 * 
 * Receives: Pipeline runtime state with tracked ordered media metadata and the essence image.
 * Does: 
 *   - Fetches the first essence image and the remaining input image collection.
 *   - Invokes FFmpeg to compile the sequence into a cohesive, length-bounded timeline video.
 * Outputs: The timeline video component without audio (written to `03_timeline.mp4`).
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
            List<Path> orderedTimings = context.getState().getOrderedMediaMetadata()
                .stream()
                .map(edu.up.cg.integrations.metadata.MediaMetadata::getSourcePath)
                .collect(Collectors.toList());
            
            List<Path> finalTimelineInput = new ArrayList<>();
            // Inject essence image as requested
            finalTimelineInput.add(context.getState().getEssenceImageFile());
            finalTimelineInput.addAll(orderedTimings);

            Path timelineVideo = context.getOutputDirectory().resolve("03_timeline.mp4");
            
            Path audioTrack = context.getState().getNarrationAudioFile();
            double duration = 0.0;
            if (audioTrack != null) {
                duration = new FFmpegCliService().analyzeAudioLoudness(audioTrack).durationSecs(); 
                // Note: The structure expects duration for timing images, so we fetch it roughly or let implementation handle it.
            }
            
            // We just assemble them
            new FFmpegCliService().createTimelineVideo(finalTimelineInput, timelineVideo, audioTrack != null ? (float)duration : 10.0f);
            
            context.getState().setTimelineVideoFile(timelineVideo);

            String details = "Visual timeline generated: " + timelineVideo;
            return new StepExecutionResult(getStage(), getStepName(), true, details);
        } catch (Exception e) {
            return new StepExecutionResult(getStage(), getStepName(), false, "Timeline assembly failed: " + e.getMessage());
        }
    }
}

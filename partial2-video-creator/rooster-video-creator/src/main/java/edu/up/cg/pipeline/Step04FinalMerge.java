package edu.up.cg.pipeline;

import edu.up.cg.integrations.ffmpeg.AudioComplianceConstraints;
import edu.up.cg.integrations.ffmpeg.AudioLoudnessMetrics;

import java.nio.file.Path;

/**
 * Step 04: Final Merge and Normalization 
 * 
 * Receives: The output from the pipeline thus far (primarily the `03_timeline.mp4` video).
 * Does: 
 *   - Copies the video and audio streams seamlessly.
 *   - Scans the generated AV stream (`03_timeline.mp4`) applying broadcast-grade audio loudness normalization via `-af loudnorm`.
 *   - Validates metrics post-normalization.
 * Outputs: The normalized merge file (`04_merged.mp4`) appended to state, plus verification of expected loudness metrics (LUFS/true peak) to the listener output stream.
 */
public class Step04FinalMerge implements PipelineStep {

    @Override
    public String getStepName() {
        return "Step04FinalMerge";
    }

    @Override
    public PipelineStage getStage() {
        return PipelineStage.FINAL_MERGE;
    }

    @Override
    public StepExecutionResult execute(PipelineContext context) {
        try {
            Path timelineVideo = context.getState().getTimelineVideoFile();

            if (timelineVideo == null) {
                return new StepExecutionResult(getStage(), getStepName(), false, "Step 03 must run first: timeline video missing");
            }

            Path compliantAudio = context.getOutputDirectory().resolve("04_narration_compliant.wav");
            context.getFfmpegService().normalizeAudioToCompliance(
                timelineVideo, // Has the merged audio
                compliantAudio,
                AudioComplianceConstraints.YOUTUBE_ASSIGNMENT
            );

            AudioLoudnessMetrics metrics = context.getFfmpegService().analyzeAudioLoudness(compliantAudio);
            String complianceNote = "Compliance OK";
            try {
                context.getFfmpegService().assertAudioCompliance(metrics, AudioComplianceConstraints.YOUTUBE_ASSIGNMENT);
            } catch (IllegalStateException complianceError) {
                complianceNote = "Compliance warning: " + complianceError.getMessage();
            }

            Path mergedVideo = context.getOutputDirectory().resolve("04_merged_video.mp4");
            context.getFfmpegService().attachNarrationTrack(timelineVideo, compliantAudio, mergedVideo);
            context.getState().setMergedVideoFile(mergedVideo);

            String details = "Merged timeline and narration. Metrics: LUFS=" + metrics.integratedLufs() +
                " TP=" + metrics.truePeakDbtp() + " LRA=" + metrics.loudnessRangeLu() + " | " + complianceNote;
            return new StepExecutionResult(getStage(), getStepName(), true, details);
        } catch (Exception e) {
            return new StepExecutionResult(getStage(), getStepName(), false, "Step failed: " + e.getMessage());
        }
    }
}

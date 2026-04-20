package edu.up.cg.integrations.ffmpeg;

import java.nio.file.Path;
import java.util.List;

public interface FFmpegService {
    void convertToPortraitCover(Path inputFile, Path outputFile, PortraitResolution resolution);

    void createStillImageVideo(Path inputImage, Path outputVideo, PortraitResolution resolution, int durationSeconds, boolean includeSilentAudio);

    void createStillImageVideoWithText(Path inputImage, Path outputVideo, String text, PortraitResolution resolution, int durationSeconds, boolean includeSilentAudio);

    void createVideoFromImageAndAudio(Path inputImage, Path inputAudio, Path outputVideo, PortraitResolution resolution);

    void concatInChronologicalOrder(List<Path> orderedMediaFiles, Path outputFile);

    void concatWithAudio(List<Path> orderedMediaFiles, Path outputFile);

    void attachNarrationTrack(Path inputVideo, Path narrationAudio, Path outputFile);

    void normalizeAudioToCompliance(Path inputAudio, Path outputAudio, AudioComplianceConstraints constraints);

    AudioLoudnessMetrics analyzeAudioLoudness(Path inputAudio);

    void assertAudioCompliance(AudioLoudnessMetrics metrics, AudioComplianceConstraints constraints);
}

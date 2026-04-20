package edu.up.cg.integrations.ffmpeg;

import edu.up.cg.integrations.common.CommandResult;
import edu.up.cg.integrations.common.CommandRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FFmpegCliService implements FFmpegService {
    private static final Pattern INPUT_I = Pattern.compile("\"input_i\"\\s*:\\s*\"?(-?\\d+(?:\\.\\d+)?)\"?");
    private static final Pattern INPUT_TP = Pattern.compile("\"input_tp\"\\s*:\\s*\"?(-?\\d+(?:\\.\\d+)?)\"?");
    private static final Pattern INPUT_LRA = Pattern.compile("\"input_lra\"\\s*:\\s*\"?(-?\\d+(?:\\.\\d+)?)\"?");
    private static final Pattern INPUT_THRESH = Pattern.compile("\"input_thresh\"\\s*:\\s*\"?(-?\\d+(?:\\.\\d+)?)\"?");
    private static final Pattern TARGET_OFFSET = Pattern.compile("\"target_offset\"\\s*:\\s*\"?(-?\\d+(?:\\.\\d+)?)\"?");

    private final CommandRunner commandRunner;

    public FFmpegCliService() {
        this(new CommandRunner());
    }

    public FFmpegCliService(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @Override
    public void convertToPortraitCover(Path inputFile, Path outputFile, PortraitResolution resolution) {
        String filter = "scale=" + resolution.width() + ":" + resolution.height() +
            ":force_original_aspect_ratio=increase,crop=" + resolution.width() + ":" + resolution.height();

        commandRunner.runOrThrow(List.of(
            "ffmpeg", "-y",
            "-i", inputFile.toAbsolutePath().toString(),
            "-vf", filter,
            "-c:v", "libx264",
            "-pix_fmt", "yuv420p",
            "-an",
            outputFile.toAbsolutePath().toString()
        ));
    }

    @Override
    public void createStillImageVideo(Path inputImage, Path outputVideo, PortraitResolution resolution, int durationSeconds, boolean includeSilentAudio) {
        String filter = "scale=" + resolution.width() + ":" + resolution.height() +
            ":force_original_aspect_ratio=increase,crop=" + resolution.width() + ":" + resolution.height();

        List<String> command = new ArrayList<>(List.of(
            "ffmpeg", "-y",
            "-loop", "1",
            "-i", inputImage.toAbsolutePath().toString(),
            "-t", String.valueOf(durationSeconds)
        ));

        if (includeSilentAudio) {
            command.addAll(List.of(
                "-f", "lavfi",
                "-i", "anullsrc=channel_layout=stereo:sample_rate=48000"
            ));
        }

        command.addAll(List.of(
            "-vf", filter,
            "-r", "30",
            "-c:v", "libx264",
            "-pix_fmt", "yuv420p"
        ));

        if (includeSilentAudio) {
            command.addAll(List.of(
                "-c:a", "aac",
                "-shortest"
            ));
        } else {
            command.add("-an");
        }

        command.add(outputVideo.toAbsolutePath().toString());
        commandRunner.runOrThrow(command);
    }

    @Override
    public void createStillImageVideoWithText(Path inputImage, Path outputVideo, String text, PortraitResolution resolution, int durationSeconds, boolean includeSilentAudio) {
        try {
            StringBuilder filterBuilder = new StringBuilder();
            filterBuilder.append("scale=").append(resolution.width()).append(":").append(resolution.height())
                .append(":force_original_aspect_ratio=increase,crop=").append(resolution.width()).append(":").append(resolution.height());

            // Process text by drawing line by line
            String[] lines = text.split("\n");
            int fontSize = 52;
            int lineHeight = 70; // rough spacing between lines
            int totalTextHeight = lines.length * lineHeight;
            int startY = resolution.height() - totalTextHeight - 150; // Place bottom-aligned, 150px margin

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                
                // Escape text for FFmpeg filter parsing: single quotes, colons, backslashes
                String escapedLine = line.replace("\\", "\\\\").replace(":", "\\:").replace("'", "'\\''");
                
                int yPos = startY + (i * lineHeight);
                
                filterBuilder.append(",drawtext=text='").append(escapedLine).append("'")
                    .append(":fontcolor=white:fontsize=").append(fontSize)
                    .append(":box=1:boxcolor=black@0.6:boxborderw=15")
                    .append(":x=(w-text_w)/2:y=").append(yPos);
            }

            String filter = filterBuilder.toString();

            List<String> command = new ArrayList<>(List.of(
                "ffmpeg", "-y",
                "-loop", "1",
                "-i", inputImage.toAbsolutePath().toString(),
                "-t", String.valueOf(durationSeconds)
            ));

            if (includeSilentAudio) {
                command.addAll(List.of(
                    "-f", "lavfi",
                    "-i", "anullsrc=channel_layout=stereo:sample_rate=48000"
                ));
            }

            command.addAll(List.of(
                "-vf", filter,
                "-r", "30",
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p"
            ));

            if (includeSilentAudio) {
                command.addAll(List.of(
                    "-c:a", "aac",
                    "-shortest"
                ));
            } else {
                command.add("-an");
            }

            command.add(outputVideo.toAbsolutePath().toString());
            commandRunner.runOrThrow(command);
        } catch (Exception e) {
            throw new RuntimeException("Failed to render text overlay", e);
        }
    }

    @Override
    public void createVideoFromImageAndAudio(Path inputImage, Path inputAudio, Path outputVideo, PortraitResolution resolution) {
        String filter = "scale=" + resolution.width() + ":" + resolution.height() +
            ":force_original_aspect_ratio=increase,crop=" + resolution.width() + ":" + resolution.height();

        commandRunner.runOrThrow(List.of(
            "ffmpeg", "-y",
            "-loop", "1",
            "-i", inputImage.toAbsolutePath().toString(),
            "-i", inputAudio.toAbsolutePath().toString(),
            "-vf", filter,
            "-r", "30",
            "-c:v", "libx264",
            "-c:a", "aac",
            "-b:a", "192k",
            "-pix_fmt", "yuv420p",
            "-shortest",
            outputVideo.toAbsolutePath().toString()
        ));
    }

    @Override
    public void concatInChronologicalOrder(List<Path> orderedMediaFiles, Path outputFile) {
        if (orderedMediaFiles == null || orderedMediaFiles.isEmpty()) {
            throw new IllegalArgumentException("orderedMediaFiles cannot be empty");
        }

        Path manifest = null;
        try {
            manifest = Files.createTempFile("ffmpeg-concat-", ".txt");
            List<String> lines = orderedMediaFiles.stream()
                .map(file -> "file '" + file.toAbsolutePath().toString().replace("'", "'\\''") + "'")
                .collect(Collectors.toList());
            Files.write(manifest, lines, StandardCharsets.UTF_8);

            commandRunner.runOrThrow(List.of(
                "ffmpeg", "-y",
                "-f", "concat",
                "-safe", "0",
                "-i", manifest.toAbsolutePath().toString(),
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                "-an",
                outputFile.toAbsolutePath().toString()
            ));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare concat manifest: " + e.getMessage(), e);
        } finally {
            if (manifest != null) {
                try {
                    Files.deleteIfExists(manifest);
                } catch (IOException ignored) {
                    // Ignore cleanup failure for temporary file.
                }
            }
        }
    }

    @Override
    public void concatWithAudio(List<Path> orderedMediaFiles, Path outputFile) {
        if (orderedMediaFiles == null || orderedMediaFiles.isEmpty()) {
            throw new IllegalArgumentException("orderedMediaFiles cannot be empty");
        }

        Path manifest = null;
        try {
            manifest = Files.createTempFile("ffmpeg-concat-audio-", ".txt");
            List<String> lines = orderedMediaFiles.stream()
                .map(file -> "file '" + file.toAbsolutePath().toString().replace("'", "'\\''") + "'")
                .collect(Collectors.toList());
            Files.write(manifest, lines, StandardCharsets.UTF_8);

            commandRunner.runOrThrow(List.of(
                "ffmpeg", "-y",
                "-f", "concat",
                "-safe", "0",
                "-i", manifest.toAbsolutePath().toString(),
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                "-c:a", "aac",
                outputFile.toAbsolutePath().toString()
            ));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare concat manifest: " + e.getMessage(), e);
        } finally {
            if (manifest != null) {
                try {
                    Files.deleteIfExists(manifest);
                } catch (IOException ignored) {
                    // Ignore cleanup failure for temporary file.
                }
            }
        }
    }

    @Override
    public void attachNarrationTrack(Path inputVideo, Path narrationAudio, Path outputFile) {
        commandRunner.runOrThrow(List.of(
            "ffmpeg", "-y",
            "-i", inputVideo.toAbsolutePath().toString(),
            "-i", narrationAudio.toAbsolutePath().toString(),
            "-map", "0:v:0",
            "-map", "1:a:0",
            "-c:v", "copy",
            "-c:a", "aac",
            "-shortest",
            outputFile.toAbsolutePath().toString()
        ));
    }

    @Override
    public void normalizeAudioToCompliance(Path inputAudio, Path outputAudio, AudioComplianceConstraints constraints) {
        LoudnormAnalysis analysis = runLoudnormAnalysis(inputAudio, constraints);

        String filter = "loudnorm=" +
            "I=" + constraints.targetLufs() + ":" +
            "TP=" + constraints.targetTruePeakDbtp() + ":" +
            "LRA=" + constraints.targetLraLu() + ":" +
            "measured_I=" + analysis.integratedLufs() + ":" +
            "measured_TP=" + analysis.truePeakDbtp() + ":" +
            "measured_LRA=" + analysis.lraLu() + ":" +
            "measured_thresh=" + analysis.threshold() + ":" +
            "offset=" + analysis.targetOffset() + ":" +
            "linear=true:print_format=summary,alimiter=limit=0.891";

        commandRunner.runOrThrow(List.of(
            "ffmpeg", "-y",
            "-i", inputAudio.toAbsolutePath().toString(),
            "-af", filter,
            "-ar", "48000",
            "-c:a", "pcm_s16le",
            outputAudio.toAbsolutePath().toString()
        ));
    }

    @Override
    public AudioLoudnessMetrics analyzeAudioLoudness(Path inputAudio) {
        LoudnormAnalysis analysis = runLoudnormAnalysis(inputAudio, AudioComplianceConstraints.YOUTUBE_ASSIGNMENT);
        return new AudioLoudnessMetrics(analysis.integratedLufs(), analysis.truePeakDbtp(), analysis.lraLu());
    }

    @Override
    public void assertAudioCompliance(AudioLoudnessMetrics metrics, AudioComplianceConstraints constraints) {
        List<String> errors = new ArrayList<>();

        if (!within(metrics.integratedLufs(), constraints.minLufs(), constraints.maxLufs())) {
            errors.add("LUFS out of range: " + metrics.integratedLufs() + " expected [" + constraints.minLufs() + ", " + constraints.maxLufs() + "]");
        }
        if (!within(metrics.truePeakDbtp(), constraints.minTruePeakDbtp(), constraints.maxTruePeakDbtp())) {
            errors.add("True peak out of range: " + metrics.truePeakDbtp() + " expected [" + constraints.minTruePeakDbtp() + ", " + constraints.maxTruePeakDbtp() + "]");
        }
        if (!within(metrics.loudnessRangeLu(), constraints.minLraLu(), constraints.maxLraLu())) {
            errors.add("LRA out of range: " + metrics.loudnessRangeLu() + " expected [" + constraints.minLraLu() + ", " + constraints.maxLraLu() + "]");
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException(String.join(" | ", errors));
        }
    }

    private LoudnormAnalysis runLoudnormAnalysis(Path inputAudio, AudioComplianceConstraints constraints) {
        String filter = "loudnorm=" +
            "I=" + constraints.targetLufs() + ":" +
            "TP=" + constraints.targetTruePeakDbtp() + ":" +
            "LRA=" + constraints.targetLraLu() + ":" +
            "print_format=json";

        CommandResult result = commandRunner.run(List.of(
            "ffmpeg",
            "-i", inputAudio.toAbsolutePath().toString(),
            "-af", filter,
            "-f", "null",
            "-"
        ));

        if (!result.isSuccess()) {
            String output = result.getOutput();
            if (output == null || !output.contains("input_i")) {
                throw new IllegalStateException("Unable to analyze loudness: " + output);
            }
        }

        String output = result.getOutput();
        double inputI = parseMetric(output, INPUT_I, "input_i");
        double inputTp = parseMetric(output, INPUT_TP, "input_tp");
        double inputLra = parseMetric(output, INPUT_LRA, "input_lra");
        double inputThresh = parseMetric(output, INPUT_THRESH, "input_thresh");
        double targetOffset = parseMetric(output, TARGET_OFFSET, "target_offset");
        return new LoudnormAnalysis(inputI, inputTp, inputLra, inputThresh, targetOffset);
    }

    private double parseMetric(String output, Pattern pattern, String key) {
        Matcher matcher = pattern.matcher(output);
        if (!matcher.find()) {
            throw new IllegalStateException("Could not parse " + key + " from loudnorm output");
        }
        return Double.parseDouble(matcher.group(1));
    }

    private boolean within(double value, double min, double max) {
        return value >= min && value <= max;
    }

    private static final class LoudnormAnalysis {
        private final double integratedLufs;
        private final double truePeakDbtp;
        private final double lraLu;
        private final double threshold;
        private final double targetOffset;

        private LoudnormAnalysis(double integratedLufs, double truePeakDbtp, double lraLu, double threshold, double targetOffset) {
            this.integratedLufs = integratedLufs;
            this.truePeakDbtp = truePeakDbtp;
            this.lraLu = lraLu;
            this.threshold = threshold;
            this.targetOffset = targetOffset;
        }

        private double integratedLufs() {
            return integratedLufs;
        }

        private double truePeakDbtp() {
            return truePeakDbtp;
        }

        private double lraLu() {
            return lraLu;
        }

        private double threshold() {
            return threshold;
        }

        private double targetOffset() {
            return targetOffset;
        }
    }
}

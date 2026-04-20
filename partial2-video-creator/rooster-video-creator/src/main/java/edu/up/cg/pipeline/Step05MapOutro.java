package edu.up.cg.pipeline;

import edu.up.cg.integrations.ffmpeg.PortraitResolution;
import edu.up.cg.integrations.metadata.GeoPoint;
import edu.up.cg.integrations.metadata.MediaMetadata;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Step 05: Map Outro and Wrap-up
 * 
 * Receives:
 *   - The entire chronological path of GPS points found from Step 01.
 *   - The previously assembled video `04_merged.mp4`.
 * Does:
 *   - Fetches a thematic Mapbox static map spanning the first & last geo-location.
 *   - Generates an inspirational wrap-up phrase via AI about the journey.
 *   - Words-wraps the quote neatly.
 *   - Constructs a still video out of the map showing the wrapped phrase text at the bottom.
 *   - Concatenates the merged video (Step 04) with the custom map video sequence silently for a smooth closing.
 * Outputs: Continues to `05_final_video.mp4`, containing the complete end-to-end trip with visual story & outro closing scene.
 */
public class Step05MapOutro implements PipelineStep {

    @Override
    public String getStepName() {
        return "Step05MapOutro";
    }

    @Override
    public PipelineStage getStage() {
        return PipelineStage.MAP_OUTRO;
    }

    @Override
    public StepExecutionResult execute(PipelineContext context) {
        try {
            List<MediaMetadata> orderedMetadata = context.getState().getOrderedMediaMetadata();
            if (orderedMetadata.isEmpty()) {
                return new StepExecutionResult(getStage(), getStepName(), false, "Step 01 must run first: no ordered metadata found");
            }

            Path mergedVideo = context.getState().getMergedVideoFile();
            if (mergedVideo == null) {
                return new StepExecutionResult(getStage(), getStepName(), false, "Step 04 must run first: merged video missing");
            }

            Optional<GeoPoint> firstLocation = orderedMetadata.stream()
                .map(MediaMetadata::getLocation)
                .flatMap(Optional::stream)
                .findFirst();

            Optional<GeoPoint> lastLocation = orderedMetadata.stream()
                .map(MediaMetadata::getLocation)
                .flatMap(Optional::stream)
                .reduce((a, b) -> b);

            if (firstLocation.isEmpty() || lastLocation.isEmpty()) {
                return new StepExecutionResult(getStage(), getStepName(), false, "At least two GPS points are required for map outro");
            }

            Path mapImage = context.getOutputDirectory().resolve("05_map.png");
            context.getMapService().downloadStaticMap(
                firstLocation.get(),
                lastLocation.get(),
                720, // Mapbox API restricts width/height to <= 1280. Ffmpeg scales this later up to 1080x1920
                1280,
                mapImage
            );
            context.getState().setMapImageFile(mapImage);

            String placesSummary = "First point: " + firstLocation.get().getLatitude() + "," + firstLocation.get().getLongitude() +
                " | Last point: " + lastLocation.get().getLatitude() + "," + lastLocation.get().getLongitude();
            String phrase = context.getAiService().generateInspirationalPhrase(placesSummary);
            
            // Basic word wrap for FFmpeg
            StringBuilder wrappedPhrase = new StringBuilder();
            int charsCounter = 0;
            for (String word : phrase.split(" ")) {
                if (charsCounter + word.length() > 25) {
                    wrappedPhrase.append("\n");
                    charsCounter = 0;
                }
                wrappedPhrase.append(word).append(" ");
                charsCounter += word.length() + 1;
            }
            phrase = wrappedPhrase.toString().trim();
            
            Path phraseFile = context.getOutputDirectory().resolve("05_inspirational_phrase.txt");
            Files.writeString(phraseFile, phrase, StandardCharsets.UTF_8);

            context.getState().setInspirationalPhrase(phrase);
            context.getState().setInspirationalPhraseFile(phraseFile);

            Path outroVideo = context.getOutputDirectory().resolve("05_map_outro.mp4");
            context.getFfmpegService().createStillImageVideoWithText(
                mapImage,
                outroVideo,
                phrase,
                PortraitResolution.HD_1080x1920,
                4,
                true
            );
            context.getState().setMapOutroVideoFile(outroVideo);

            Path finalVideo = context.getOutputDirectory().resolve("05_final_video.mp4");
            context.getFfmpegService().concatWithAudio(List.of(mergedVideo, outroVideo), finalVideo);
            context.getState().setFinalVideoFile(finalVideo);

            return new StepExecutionResult(getStage(), getStepName(), true, "Final video generated: " + finalVideo);
        } catch (Exception e) {
            return new StepExecutionResult(getStage(), getStepName(), false, "Step failed: " + e.getMessage());
        }
    }
}
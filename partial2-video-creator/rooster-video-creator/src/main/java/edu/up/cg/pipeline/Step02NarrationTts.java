package edu.up.cg.pipeline;

import edu.up.cg.integrations.common.CommandResult;
import edu.up.cg.integrations.metadata.MediaMetadata;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Step 02: Narration & TTS Generation
 * 
 * Receives: An ordered list of pipeline media files, and their geo-metadata context.
 * Does: 
 *   - Compiles a prompt containing descriptions/coordinates of each chronological location.
 *   - Calls the AI service returning distinct sentences mapped to locations separated by pipes.
 *   - Separates spoken fragments and streams them into Google Translate TTS individually.
 * Outputs: The entire textual voice script and a list of paths (`02_narration_XXX.mp3`) containing parsed voice bytes loaded onto state map.
 */
public class Step02NarrationTts implements PipelineStep {

    @Override
    public String getStepName() {
        return "Step02NarrationTts";
    }

    @Override
    public PipelineStage getStage() {
        return PipelineStage.NARRATION_TTS;
    }

    @Override
    public StepExecutionResult execute(PipelineContext context) {
        try {
            List<MediaMetadata> orderedMetadata = context.getState().getOrderedMediaMetadata();
            if (orderedMetadata.isEmpty()) {
                return new StepExecutionResult(getStage(), getStepName(), false, "Step 01 must run first: no ordered metadata found");
            }

            String timelineSummary = buildTimelineSummary(orderedMetadata);
            String narrationScript = context.getAiService().generateNarrationScript(timelineSummary);

            Path scriptFile = context.getOutputDirectory().resolve("02_narration_script.txt");
            Files.writeString(scriptFile, narrationScript, StandardCharsets.UTF_8);

            List<Path> individualAudios = new ArrayList<>();
            String[] segments = narrationScript.split("\\|");
            for (int i = 0; i < orderedMetadata.size(); i++) {
                String textSegment = i < segments.length ? segments[i].trim() : "Journey continues...";
                if (textSegment.isEmpty()) {
                    textSegment = "Journey continues...";
                }
                Path narrationAudio = context.getOutputDirectory().resolve(String.format("02_narration_%03d.mp3", i + 1));
                createNarrationAudio(context, textSegment, narrationAudio);
                individualAudios.add(narrationAudio);
            }

            context.getState().setNarrationScript(narrationScript);
            context.getState().setNarrationScriptFile(scriptFile);
            context.getState().setIndividualNarrationAudios(individualAudios);

            return new StepExecutionResult(getStage(), getStepName(), true, "Narration script and individual audio files generated");
        } catch (Exception e) {
            return new StepExecutionResult(getStage(), getStepName(), false, "Step failed: " + e.getMessage());
        }
    }

    private String buildTimelineSummary(List<MediaMetadata> orderedMetadata) {
        StringBuilder summary = new StringBuilder("Create narration for this chronological media timeline. Identify the geographical places from these coordinates and vividly describe traveling between them:")
            .append(System.lineSeparator());

        for (int i = 0; i < orderedMetadata.size(); i++) {
            MediaMetadata metadata = orderedMetadata.get(i);
            summary.append(i + 1)
                .append(". ")
                .append(metadata.getMediaType())
                .append(" at ")
                .append(metadata.getCapturedAt().map(Object::toString).orElse("unknown date"))
                .append(" in ")
                .append(metadata.getLocation().map(value -> "Latitude: " + value.getLatitude() + ", Longitude: " + value.getLongitude()).orElse("unknown location"))
                .append(System.lineSeparator());
        }
        return summary.toString();
    }

    private void createNarrationAudio(PipelineContext context, String narrationScript, Path narrationAudio) {
        if (tryGoogleTranslateTts(narrationScript, narrationAudio)) {
            return;
        }

        if (tryEspeak(context, "espeak-ng", narrationScript, narrationAudio)) {
            return;
        }
        if (tryEspeak(context, "espeak", narrationScript, narrationAudio)) {
            return;
        }

        context.getCommandRunner().runOrThrow(List.of(
            "ffmpeg", "-y",
            "-f", "lavfi",
            "-i", "anullsrc=channel_layout=stereo:sample_rate=48000",
            "-t", "4",
            narrationAudio.toAbsolutePath().toString()
        ));
    }

    private boolean tryEspeak(PipelineContext context, String binary, String narrationScript, Path narrationAudio) {
        CommandResult whichResult = context.getCommandRunner().run("which", binary);
        if (!whichResult.isSuccess()) {
            return false;
        }

        CommandResult speechResult = context.getCommandRunner().run(
            binary,
            "-v", "en-us",
            "-s", "140",
            "-p", "32",
            "-g", "14",
            "-a", "115",
            "-w",
            narrationAudio.toAbsolutePath().toString(),
            narrationScript
        );
        return speechResult.isSuccess();
    }

    private boolean tryGoogleTranslateTts(String narrationScript, Path narrationAudio) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            List<String> words = List.of(narrationScript.split("\\s+"));
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            StringBuilder chunk = new StringBuilder();
            for (String word : words) {
                if (chunk.length() + word.length() > 150) {
                    downloadTtsChunk(client, chunk.toString(), out);
                    chunk.setLength(0);
                }
                chunk.append(word).append(" ");
            }
            if (chunk.length() > 0) {
                downloadTtsChunk(client, chunk.toString(), out);
            }

            if (out.size() > 0) {
                Files.write(narrationAudio, out.toByteArray());
                return true;
            }
        } catch (Exception e) {
            System.err.println("Google TTS fetch failed: " + e.getMessage());
        }
        return false;
    }

    private void downloadTtsChunk(HttpClient client, String text, ByteArrayOutputStream out) throws Exception {
        String encoded = URLEncoder.encode(text.trim(), StandardCharsets.UTF_8);
        URI uri = URI.create("https://translate.google.com/translate_tts?ie=UTF-8&q=" + encoded + "&tl=en&client=tw-ob");
        HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<byte[]> res = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (res.statusCode() == 200) {
            out.write(res.body());
        } else {
            throw new RuntimeException("HTTP " + res.statusCode());
        }
    }
}

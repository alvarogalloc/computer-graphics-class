package edu.up.cg.pipeline;

import edu.up.cg.integrations.metadata.MediaMetadata;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PipelineRuntimeState {
    private final List<MediaMetadata> orderedMediaMetadata;
    private String essencePrompt;
    private Path essencePromptFile;
    private Path essenceImageFile;
    private Path timelineVideoFile;
    private String narrationScript;
    private Path narrationScriptFile;
    private Path narrationAudioFile;
    private List<Path> individualNarrationAudios = new ArrayList<>();
    private Path mergedVideoFile;
    private String inspirationalPhrase;
    private Path inspirationalPhraseFile;
    private Path mapImageFile;
    private Path mapOutroVideoFile;
    private Path finalVideoFile;

    public PipelineRuntimeState() {
        this.orderedMediaMetadata = new ArrayList<>();
    }

    public List<MediaMetadata> getOrderedMediaMetadata() {
        return orderedMediaMetadata;
    }

    public String getEssencePrompt() {
        return essencePrompt;
    }

    public void setEssencePrompt(String essencePrompt) {
        this.essencePrompt = essencePrompt;
    }

    public Path getEssencePromptFile() {
        return essencePromptFile;
    }

    public void setEssencePromptFile(Path essencePromptFile) {
        this.essencePromptFile = essencePromptFile;
    }

    public Path getEssenceImageFile() {
        return essenceImageFile;
    }

    public void setEssenceImageFile(Path essenceImageFile) {
        this.essenceImageFile = essenceImageFile;
    }

    public Path getTimelineVideoFile() {
        return timelineVideoFile;
    }

    public void setTimelineVideoFile(Path timelineVideoFile) {
        this.timelineVideoFile = timelineVideoFile;
    }

    public String getNarrationScript() {
        return narrationScript;
    }

    public void setNarrationScript(String narrationScript) {
        this.narrationScript = narrationScript;
    }

    public Path getNarrationScriptFile() {
        return narrationScriptFile;
    }

    public void setNarrationScriptFile(Path narrationScriptFile) {
        this.narrationScriptFile = narrationScriptFile;
    }

    public Path getNarrationAudioFile() {
        return narrationAudioFile;
    }

    public void setNarrationAudioFile(Path narrationAudioFile) {
        this.narrationAudioFile = narrationAudioFile;
    }

    public List<Path> getIndividualNarrationAudios() {
        return individualNarrationAudios;
    }

    public void setIndividualNarrationAudios(List<Path> audios) {
        this.individualNarrationAudios = new ArrayList<>(audios);
    }

    public Path getMergedVideoFile() {
        return mergedVideoFile;
    }

    public void setMergedVideoFile(Path mergedVideoFile) {
        this.mergedVideoFile = mergedVideoFile;
    }

    public String getInspirationalPhrase() {
        return inspirationalPhrase;
    }

    public void setInspirationalPhrase(String inspirationalPhrase) {
        this.inspirationalPhrase = inspirationalPhrase;
    }

    public Path getInspirationalPhraseFile() {
        return inspirationalPhraseFile;
    }

    public void setInspirationalPhraseFile(Path inspirationalPhraseFile) {
        this.inspirationalPhraseFile = inspirationalPhraseFile;
    }

    public Path getMapImageFile() {
        return mapImageFile;
    }

    public void setMapImageFile(Path mapImageFile) {
        this.mapImageFile = mapImageFile;
    }

    public Path getMapOutroVideoFile() {
        return mapOutroVideoFile;
    }

    public void setMapOutroVideoFile(Path mapOutroVideoFile) {
        this.mapOutroVideoFile = mapOutroVideoFile;
    }

    public Path getFinalVideoFile() {
        return finalVideoFile;
    }

    public void setFinalVideoFile(Path finalVideoFile) {
        this.finalVideoFile = finalVideoFile;
    }
}

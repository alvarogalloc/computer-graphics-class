package edu.up.cg.pipeline;

import edu.up.cg.integrations.ai.AIService;
import edu.up.cg.integrations.common.CommandRunner;
import edu.up.cg.integrations.ffmpeg.FFmpegService;
import edu.up.cg.integrations.map.MapService;
import edu.up.cg.integrations.metadata.MetadataService;

import java.nio.file.Path;
import java.util.List;

public final class PipelineContext {
    private final List<Path> sourceMedia;
    private final Path outputDirectory;
    private final MetadataService metadataService;
    private final AIService aiService;
    private final FFmpegService ffmpegService;
    private final MapService mapService;
    private final CommandRunner commandRunner;
    private final PipelineRuntimeState state;

    public PipelineContext(
        List<Path> sourceMedia,
        Path outputDirectory,
        MetadataService metadataService,
        AIService aiService,
        FFmpegService ffmpegService,
        MapService mapService,
        CommandRunner commandRunner,
        PipelineRuntimeState state
    ) {
        this.sourceMedia = List.copyOf(sourceMedia);
        this.outputDirectory = outputDirectory;
        this.metadataService = metadataService;
        this.aiService = aiService;
        this.ffmpegService = ffmpegService;
        this.mapService = mapService;
        this.commandRunner = commandRunner;
        this.state = state;
    }

    public List<Path> getSourceMedia() {
        return sourceMedia;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public MetadataService getMetadataService() {
        return metadataService;
    }

    public AIService getAiService() {
        return aiService;
    }

    public FFmpegService getFfmpegService() {
        return ffmpegService;
    }

    public MapService getMapService() {
        return mapService;
    }

    public CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public PipelineRuntimeState getState() {
        return state;
    }
}

package edu.up.cg.integrations.ffmpeg;

import edu.up.cg.health.ServiceHealth;
import edu.up.cg.integrations.common.CommandRunner;

public class FFmpegCliService implements FFmpegService {
    private final CommandRunner commandRunner;

    public FFmpegCliService() {
        this(new CommandRunner());
    }

    public FFmpegCliService(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @Override
    public ServiceHealth healthCheck() {
        return commandRunner.checkCommand("FFmpeg", "ffmpeg is available", "ffmpeg", "-version");
    }
}

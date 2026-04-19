package edu.up.cg.integrations.exiftool;

import edu.up.cg.health.ServiceHealth;
import edu.up.cg.integrations.common.CommandRunner;

public class ExifToolCliService implements ExifToolService {
    private final CommandRunner commandRunner;

    public ExifToolCliService() {
        this(new CommandRunner());
    }

    public ExifToolCliService(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @Override
    public ServiceHealth healthCheck() {
        return commandRunner.checkCommand("ExifTool", "exiftool is available", "exiftool", "-ver");
    }
}

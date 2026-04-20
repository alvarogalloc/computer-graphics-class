package edu.up.cg.integrations.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;

public class CommandRunner {

    public CommandResult run(String... command) {
        return run(List.of(command));
    }

    public CommandResult run(List<String> command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                StringJoiner joiner = new StringJoiner("\n");
                String line;
                while ((line = reader.readLine()) != null) {
                    joiner.add(line);
                }
                output = joiner.toString();
            }
            int code = process.waitFor();
            return new CommandResult(code, output);
        } catch (IOException e) {
            return new CommandResult(127, "Command not found or not executable: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new CommandResult(130, "Command interrupted");
        }
    }

    public void runOrThrow(List<String> command) {
        CommandResult result = run(command);
        if (!result.isSuccess()) {
            throw new IllegalStateException(
                "Command failed (" + result.getExitCode() + "): " + String.join(" ", command) + " -> " + summarize(result.getOutput())
            );
        }
    }

    private String summarize(String output) {
        if (output == null) {
            return "No output";
        }
        String trimmed = output.trim();
        return trimmed.length() <= 200 ? trimmed : trimmed.substring(0, 200) + "...";
    }
}

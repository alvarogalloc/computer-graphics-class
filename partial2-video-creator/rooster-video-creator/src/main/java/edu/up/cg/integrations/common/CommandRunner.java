package edu.up.cg.integrations.common;

import edu.up.cg.health.ServiceHealth;

import java.io.IOException;

public class CommandRunner {

    public ServiceHealth checkCommand(String serviceName, String successDetails, String... command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            int code = process.waitFor();

            if (code == 0) {
                return ServiceHealth.healthy(serviceName, successDetails);
            }
            return ServiceHealth.unhealthy(serviceName, "Command exited with code " + code);
        } catch (IOException e) {
            return ServiceHealth.unhealthy(serviceName, "Command not found or not executable: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ServiceHealth.unhealthy(serviceName, "Health check interrupted");
        }
    }
}

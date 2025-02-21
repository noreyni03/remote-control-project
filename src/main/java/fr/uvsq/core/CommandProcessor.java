package fr.uvsq.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class CommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    public String executeCommand(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb.command("cmd.exe", "/c", command);
            } else {
                pb.command("sh", "-c", command);
            }

            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);

            if (!finished) {
                process.destroy();
                return "❌ Command timed out after 5 seconds";
            }

            return readOutput(process);
        } catch (Exception e) {
            logger.error("Command execution failed: {}", e.getMessage(), e);
            return "⚠️ Error: " + e.getMessage();
        }
    }

    private String readOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) output.append(line).append("\n");
            while ((line = errorReader.readLine()) != null) output.append("[ERROR] ").append(line).append("\n");
        }
        return output.toString().trim();
    }
}
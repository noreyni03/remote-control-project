package fr.uvsq.core;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class CommandProcessor {
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
                return "Command timed out";
            }
            
            return readOutput(process);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String readOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            
            String line;
            while ((line = reader.readLine()) != null) output.append(line).append("\n");
            while ((line = errorReader.readLine()) != null) output.append("[ERROR] ").append(line).append("\n");
        }
        return output.toString().trim();
    }
}
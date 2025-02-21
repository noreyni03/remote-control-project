package fr.uvsq.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Exécute les commandes système et capture leur sortie.
 */
public class CommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    /**
     * Exécute une commande système et retourne sa sortie.
     *
     * @param command La commande à exécuter.
     * @return La sortie de la commande.
     */
    public String executeCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                processBuilder.command("cmd.exe", "/c", command);
            } else {
                processBuilder.command("sh", "-c", command);
            }

            Process process = processBuilder.start();
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

    private String readOutput(Process process) throws Exception {
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
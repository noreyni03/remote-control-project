package fr.uvsq.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * La classe `CommandProcessor` est responsable de l'exécution des commandes du système d'exploitation.
 * Elle prend une commande en entrée et exécute cette commande sur le système sous-jacent.
 * Elle gère également les erreurs potentielles lors de l'exécution de la commande et retourne la sortie
 * ou les erreurs générées par la commande.
 */
public class CommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    /**
     * Exécute une commande système.
     *
     * @param command La commande à exécuter.
     * @return La sortie de la commande (stdout et stderr combinés) ou un message d'erreur en cas d'échec.
     */
    public String executeCommand(String command) {
        try {
            // Création d'un constructeur de processus.
            ProcessBuilder pb = new ProcessBuilder();

            // Détermination du système d'exploitation et définition de la commande en conséquence.
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Pour Windows, on utilise cmd.exe avec l'option /c pour exécuter la commande.
                pb.command("cmd.exe", "/c", command);
            } else {
                // Pour les autres systèmes (Linux, macOS), on utilise sh avec l'option -c pour exécuter la commande.
                pb.command("sh", "-c", command);
            }

            // Démarrage du processus.
            Process process = pb.start();

            // Attente de la fin du processus avec un timeout de 5 secondes.
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);

            // Si le processus n'a pas terminé dans le temps imparti, on le détruit et on retourne un message d'erreur.
            if (!finished) {
                process.destroy();
                return "❌ La commande a expiré après 5 secondes";
            }

            // Lecture de la sortie du processus.
            return readOutput(process);

        } catch (Exception e) {
            // En cas d'erreur, on log l'erreur et on retourne un message d'erreur.
            logger.error("L'exécution de la commande a échoué: {}", e.getMessage(), e);
            return "⚠️ Erreur: " + e.getMessage();
        }
    }

    /**
     * Lit la sortie (stdout et stderr) d'un processus.
     *
     * @param process Le processus dont on doit lire la sortie.
     * @return La sortie du processus sous forme de chaîne de caractères.
     * @throws IOException Si une erreur d'entrée/sortie se produit lors de la lecture.
     */
    private String readOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();

        // Utilisation de try-with-resources pour s'assurer de la fermeture des lecteurs.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {

            String line;
            // Lecture de la sortie standard (stdout).
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Lecture de la sortie d'erreur (stderr).
            while ((line = errorReader.readLine()) != null) {
                output.append("[ERROR] ").append(line).append("\n");
            }
        }
        // Retourne la sortie formatée, en supprimant les espaces superflus au début et à la fin.
        return output.toString().trim();
    }
}
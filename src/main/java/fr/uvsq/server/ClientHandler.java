package fr.uvsq.server;

import fr.uvsq.core.CommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * La classe `ClientHandler` gère la communication avec un client connecté au serveur.
 * Elle implémente l'interface `Runnable` pour pouvoir être exécutée dans un thread séparé,
 * permettant ainsi de gérer plusieurs clients simultanément.
 *
 * Chaque instance de `ClientHandler` est responsable de :
 * - La réception des commandes envoyées par un client.
 * - L'exécution de ces commandes via un `CommandProcessor`.
 * - L'envoi des résultats de l'exécution au client.
 * - La gestion des erreurs de communication et la déconnexion du client.
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final String END_MARKER = "###END###";

    private final Socket clientSocket;
    private final CommandProcessor processor = new CommandProcessor();
    private final Consumer<String> logCallback;

    /**
     * Constructeur de `ClientHandler`.
     *
     * @param socket      Le socket de communication avec le client.
     * @param logCallback Une fonction de rappel (callback) pour l'affichage des logs.
     *                    Cette fonction prend une chaîne de caractères (le message de log) en entrée.
     */
    public ClientHandler(Socket socket, Consumer<String> logCallback) {
        this.clientSocket = socket;
        this.logCallback = logCallback;
    }

    /**
     * Méthode exécutée par le thread lorsque le `ClientHandler` est démarré.
     * Gère la communication avec le client :
     * - Établit les flux d'entrée/sortie pour la communication.
     * - Récupère l'identifiant du client (adresse IP et port).
     * - Boucle pour lire les commandes envoyées par le client.
     * - Exécute les commandes via le `CommandProcessor`.
     * - Envoie la réponse au client, suivie du marqueur de fin `END_MARKER`.
     * - Gère les erreurs de communication.
     * - Gère la déconnexion du client.
     */
    @Override
    public void run() {
        // Utilisation de try-with-resources pour s'assurer que les flux sont bien fermés à la fin.
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String clientId = clientSocket.getInetAddress() + ":" + clientSocket.getPort();
            logCallback.accept("📩 Handling client: " + clientId);

            String command;
            // Lecture des commandes envoyées par le client jusqu'à ce que la connexion soit coupée.
            while ((command = in.readLine()) != null) {
                logCallback.accept("Received command: " + command);
                // Exécution de la commande et récupération de la réponse.
                String response = processor.executeCommand(command);
                // Envoi de la réponse au client.
                out.println(response);
                // Envoi du marqueur de fin.
                out.println(END_MARKER);
            }
        } catch (Exception e) {
            // Gestion des erreurs de communication.
            logCallback.accept("⚠️ Client connection error: " + e.getMessage());
        } finally {
            // Gestion de la déconnexion du client.
            logCallback.accept("🔌 Client disconnected");
        }
    }
}
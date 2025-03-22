package fr.uvsq.server;

import fr.uvsq.core.CommandProcessor;
import fr.uvsq.core.AuthManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    private final AuthManager authManager = new AuthManager(); // Ajout de l'AuthManager
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
     * - Vérifie l'authentification avant de traiter les commandes.
     * - Boucle pour lire les commandes envoyées par le client.
     * - Exécute les commandes via le `CommandProcessor`.
     * - Envoie la réponse au client, suivie du marqueur de fin `END_MARKER`.
     * - Gère les erreurs de communication.
     * - Gère la déconnexion du client.
     */

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String clientId = clientSocket.getInetAddress() + ":" + clientSocket.getPort();
            logCallback.accept("📩 Handling client: " + clientId);

            String authSignal = in.readLine();
            if (!"AUTH".equals(authSignal)) {
                logCallback.accept("⚠️ Client " + clientId + " n'a pas envoyé AUTH.");
                out.println("ERROR: Authentification requise.");
                return;
            }

            String login = in.readLine();
            String password = in.readLine();
            if (authManager.authenticate(login, password)) {
                logCallback.accept("✅ Client " + clientId + " authentifié avec succès.");
                out.println("OK");
            } else {
                logCallback.accept("❌ Échec de l'authentification pour " + clientId);
                out.println("ERROR: Identifiants incorrects.");
                return;
            }

            String command;
            while ((command = in.readLine()) != null) {
                logCallback.accept("Received command: " + command);

                if ("UPLOAD".equals(command)) {
                    String fileName = in.readLine();
                    long fileSize = Long.parseLong(in.readLine());
                    String savePath = "server_files/" + fileName;
                    Files.createDirectories(Paths.get("server_files")); // Crée le dossier si inexistant
                    try (FileOutputStream fos = new FileOutputStream(savePath)) {
                        byte[] buffer = new byte[4096];
                        long bytesReceived = 0;
                        int bytesRead;
                        InputStream inputStream = clientSocket.getInputStream();
                        while (bytesReceived < fileSize && (bytesRead = inputStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            bytesReceived += bytesRead;
                        }
                        fos.flush();
                    }
                    out.println("OK");
                    logCallback.accept("📤 Fichier reçu : " + fileName);
                } else if ("DOWNLOAD".equals(command)) {
                    String fileName = in.readLine();
                    File file = new File("server_files/" + fileName);
                    if (file.exists()) {
                        out.println(file.length());
                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                clientSocket.getOutputStream().write(buffer, 0, bytesRead);
                            }
                            clientSocket.getOutputStream().flush();
                        }
                    } else {
                        out.println(-1);
                        out.println("Fichier non trouvé : " + fileName);
                    }
                    logCallback.accept("📥 Fichier demandé : " + fileName);
                } else {
                    String response = processor.executeCommand(command);
                    out.println(response);
                    out.println(END_MARKER);
                }
            }
        } catch (Exception e) {
            logCallback.accept("⚠️ Client connection error: " + e.getMessage());
        } finally {
            logCallback.accept("🔌 Client disconnected");
        }
    }

}
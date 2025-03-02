package fr.uvsq.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * La classe `Server` représente le serveur principal pour le système de contrôle à distance.
 * Elle est responsable de l'écoute des connexions entrantes, de la gestion d'un pool de threads
 * pour traiter les clients simultanément et de la gestion des callbacks pour la journalisation et
 * la mise à jour de la liste des clients.
 */
public class Server {
    /**
     * Indique si le serveur est en cours d'exécution ou non.
     * `volatile` assure que la valeur de cette variable est toujours lue depuis la mémoire principale.
     */
    private volatile boolean running = true;

    /**
     * Fonction de rappel (callback) pour la journalisation des événements du serveur.
     * Elle prend une chaîne de caractères (le message de log) en entrée.
     */
    private Consumer<String> logCallback;

    /**
     * Fonction de rappel (callback) pour la gestion des nouveaux clients connectés.
     * Elle prend une chaîne de caractères (l'identifiant du client) en entrée.
     */
    private Consumer<String> clientCallback;

    /**
     * Le pool de threads utilisé pour gérer les clients connectés de manière concurrente.
     */
    private ExecutorService threadPool;

    /**
     * Définit la fonction de rappel pour la journalisation des événements du serveur.
     *
     * @param logCallback La fonction de rappel pour la journalisation.
     */
    public void setLogCallback(Consumer<String> logCallback) {
        this.logCallback = logCallback;
    }

    /**
     * Définit la fonction de rappel pour la gestion des clients connectés.
     *
     * @param clientCallback La fonction de rappel pour la gestion des clients.
     */
    public void setClientCallback(Consumer<String> clientCallback) {
        this.clientCallback = clientCallback;
    }

    /**
     * Démarre le serveur et commence à écouter les connexions entrantes.
     * Gère l'acceptation des clients et les délègue à des `ClientHandler` dans un pool de threads.
     */
    public void start() {
        final int PORT = 5001;
        final int MAX_CLIENTS = 10;
        // Initialisation du pool de threads avec un nombre fixe de threads.
        threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        // Utilisation de try-with-resources pour assurer la fermeture du ServerSocket.
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Journalisation du démarrage du serveur.
            logCallback.accept("✅ Server listening on port " + PORT);
            // Boucle d'écoute des connexions entrantes tant que le serveur est en cours d'exécution.
            while (running) {
                // Accepte une nouvelle connexion cliente.
                Socket socket = serverSocket.accept();
                // Récupération des informations du client (adresse IP et port).
                String clientInfo = socket.getInetAddress() + ":" + socket.getPort();
                // Appel du callback pour informer de la connexion du client.
                clientCallback.accept(clientInfo);
                // Délégation de la gestion du client à un ClientHandler dans le pool de threads.
                threadPool.execute(new ClientHandler(socket, logCallback));
            }
        } catch (Exception e) {
            // Journalisation des erreurs survenues lors de l'exécution du serveur.
            logCallback.accept("❌ Server error: " + e.getMessage());
        }
    }

    /**
     * Arrête le serveur et ferme le pool de threads.
     */
    public void stop() {
        // Indique au serveur d'arrêter sa boucle d'écoute.
        running = false;
        // Fermeture du pool de threads si celui-ci est initialisé.
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }
}
package fr.uvsq.server;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * La classe `Server` représente le serveur principal de l'application de contrôle à distance.
 * Elle gère l'écoute des connexions entrantes des clients, l'authentification,
 * et la gestion des threads pour chaque client connecté.
 * Le serveur utilise SSL pour sécuriser les communications.
 */
public class Server {
    private volatile boolean running = true;
    private Consumer<String> logCallback;
    private Consumer<String> clientCallback;
    private Consumer<String> disconnectCallback;
    private ExecutorService threadPool;
    private final CopyOnWriteArrayList<String> connectedClients = new CopyOnWriteArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    /**
     * Définit la fonction de rappel (callback) pour la journalisation (logging).
     *
     * @param logCallback La fonction de rappel qui prend une chaîne de caractères (le message de log) en entrée.
     */
    public void setLogCallback(Consumer<String> logCallback) {
        this.logCallback = logCallback;
    }

    /**
     * Définit la fonction de rappel (callback) pour la connexion d'un nouveau client.
     *
     * @param clientCallback La fonction de rappel qui prend une chaîne de caractères (l'identifiant du client) en entrée.
     */
    public void setClientCallback(Consumer<String> clientCallback) {
        this.clientCallback = clientCallback;
    }

    /**
     * Définit la fonction de rappel (callback) pour la dnexion d'un client.
     *
     * @param disconnectCallback La fonction de rappel qui prend une chaîne de caractères (l'identifiant du client) en entrée.
     */
    public void setDisconnectCallback(Consumer<String> disconnectCallback) {
        this.disconnectCallback = disconnectCallback;
    }

    /**
     * Rrn Une liste thread-safe des identifiants des clients connectés.
     */
    public CopyOnWriteArrayList<String> getConnectedClients() {
        return connectedClients;
    }

    /**
     * Démarre le serveur et commence à écouter les connexions entrantes.
     * Le serveur utilise un pool de threads pour gérer les clients simultanément.
     * Il utilise SSL pour sécuriser les communications.
     */
    public void start() {
        final int PORT = 5001;
        final int MAX_CLIENTS = 10;
        threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try {
            // Configuration du keystore SSL
            System.setProperty("javax.net.ssl.keyStore", "server_keystore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "password");

            // Création d'une socket serveur SSL
            SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);

            logger.info("Server started on port {} with SSL", PORT);
            logCallback.accept("✅ Server listening on port " + PORT + " with SSL");
            // Boucle principale du serveur
            while (running) {
                // Accepte une nouvelle connexion
                Socket socket = serverSocket.accept();
                String clientInfo = socket.getInetAddress() + ":" + socket.getPort();
                // Ajoute le client à la liste des clients connectés
                connectedClients.add(clientInfo);
                logger.info("New client connected: {}", clientInfo);
                logCallback.accept("📩 Nouveau client connecté : " + clientInfo);
                clientCallback.accept(clientInfo);
                // Exécute le ClientHandler dans un thread séparé
                threadPool.execute(new ClientHandler(socket, logCallback) {
                    @Override
                    public void run() {
                        // Exécution du ClientHandler
                        super.run();
                        // Suppression du client de la liste des clients connectés à la fin de la connexion
                        connectedClients.remove(clientInfo);
                        logger.info("Client disconnected: {}", clientInfo);
                        logCallback.accept("🔌 Client déconnecté : " + clientInfo);
                        // Appel du callback de déconnexion si défini
                        if (disconnectCallback != null) {
                            disconnectCallback.accept(clientInfo);
                        }
                    }
                });
            }
            serverSocket.close();
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            logCallback.accept("❌ Server error: " + e.getMessage());
        }
    }

    /**
     * Arrête le serveur.
     * Arrête l'écoute des connexions et ferme le pool de threads.
     */
    public void stop() {
        running = false;
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }
}
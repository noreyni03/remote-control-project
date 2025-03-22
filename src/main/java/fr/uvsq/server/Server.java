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

public class Server {
    private volatile boolean running = true;
    private Consumer<String> logCallback;
    private Consumer<String> clientCallback;
    private Consumer<String> disconnectCallback;
    private ExecutorService threadPool;
    private final CopyOnWriteArrayList<String> connectedClients = new CopyOnWriteArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public void setLogCallback(Consumer<String> logCallback) {
        this.logCallback = logCallback;
    }

    public void setClientCallback(Consumer<String> clientCallback) {
        this.clientCallback = clientCallback;
    }

    public void setDisconnectCallback(Consumer<String> disconnectCallback) {
        this.disconnectCallback = disconnectCallback;
    }

    public CopyOnWriteArrayList<String> getConnectedClients() {
        return connectedClients;
    }

    public void start() {
        final int PORT = 5001;
        final int MAX_CLIENTS = 10;
        threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try {
            System.setProperty("javax.net.ssl.keyStore", "server_keystore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "password");

            SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);

            logger.info("Server started on port {} with SSL", PORT);
            logCallback.accept("✅ Server listening on port " + PORT + " with SSL");
            while (running) {
                Socket socket = serverSocket.accept();
                String clientInfo = socket.getInetAddress() + ":" + socket.getPort();
                connectedClients.add(clientInfo);
                logger.info("New client connected: {}", clientInfo);
                logCallback.accept("📩 Nouveau client connecté : " + clientInfo);
                clientCallback.accept(clientInfo);
                threadPool.execute(new ClientHandler(socket, logCallback) {
                    @Override
                    public void run() {
                        super.run();
                        connectedClients.remove(clientInfo);
                        logger.info("Client disconnected: {}", clientInfo);
                        logCallback.accept("🔌 Client déconnecté : " + clientInfo);
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

    public void stop() {
        running = false;
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }
}
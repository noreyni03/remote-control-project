package fr.uvsq.server;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private volatile boolean running = true;
    private Consumer<String> logCallback;
    private Consumer<String> clientCallback;
    private Consumer<String> disconnectCallback;
    private ExecutorService threadPool;
    private final CopyOnWriteArrayList<String> connectedClients = new CopyOnWriteArrayList<>();

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

            logCallback.accept("✅ Server listening on port " + PORT + " with SSL");
            while (running) {
                Socket socket = serverSocket.accept();
                String clientInfo = socket.getInetAddress() + ":" + socket.getPort();
                connectedClients.add(clientInfo);
                logCallback.accept("📩 Nouveau client connecté : " + clientInfo);
                clientCallback.accept(clientInfo);
                threadPool.execute(new ClientHandler(socket, logCallback) {
                    @Override
                    public void run() {
                        super.run();
                        connectedClients.remove(clientInfo);
                        logCallback.accept("🔌 Client déconnecté : " + clientInfo);
                        if (disconnectCallback != null) {
                            disconnectCallback.accept(clientInfo);
                        }
                    }
                });
            }
            serverSocket.close();
        } catch (Exception e) {
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
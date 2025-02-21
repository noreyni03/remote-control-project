package fr.uvsq.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 5001;
    private static final int MAX_CLIENTS = 10;

    public static void main(String[] args) {
        logger.info("🚀 Starting server on port {}...", PORT);

        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("✅ Server is listening on port {}", PORT);

            while (!Thread.currentThread().isInterrupted()) {
                threadPool.execute(new ClientHandler(serverSocket.accept()));
                logger.info("🔌 New client connected");
            }
        } catch (Exception e) {
            logger.error("❌ Server error: {}", e.getMessage(), e);
        } finally {
            threadPool.shutdown();
            logger.info("🛑 Server shutdown complete");
        }
    }
}
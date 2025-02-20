package fr.uvsq.server;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 5001;
    private static final int MAX_CLIENTS = 10;

    public static void main(String[] args) throws Exception {
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🚀 Server started on port " + PORT);
            
            while (true) {
                threadPool.execute(new ClientHandler(serverSocket.accept()));
            }
        }
    }
}
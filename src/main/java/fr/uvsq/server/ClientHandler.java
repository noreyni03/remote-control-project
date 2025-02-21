package fr.uvsq.server;

import fr.uvsq.core.CommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final String END_MARKER = "###END###";

    private final Socket clientSocket;
    private final CommandProcessor processor = new CommandProcessor();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String clientId = clientSocket.getInetAddress() + ":" + clientSocket.getPort();
            logger.info("📩 Handling client: {}", clientId);

            String command;
            while ((command = in.readLine()) != null) {
                logger.debug("Received command: {}", command);
                String response = processor.executeCommand(command);
                out.println(response);
                out.println(END_MARKER);
            }
        } catch (Exception e) {
            logger.warn("⚠️ Client connection error: {}", e.getMessage(), e);
        } finally {
            logger.info("🔌 Client disconnected");
        }
    }
}
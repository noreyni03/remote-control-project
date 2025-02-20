package fr.uvsq.server;

import fr.uvsq.core.CommandProcessor;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final CommandProcessor processor = new CommandProcessor();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String command;
            while ((command = in.readLine()) != null) {
                String response = processor.executeCommand(command);
                out.println(response);
                out.println("###END###");
            }
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}
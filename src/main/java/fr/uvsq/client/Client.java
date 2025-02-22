package fr.uvsq.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Client(String host, int port) throws IOException {
        connect(host, port);
    }

    private void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                true
        );
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
        );
    }

    public String sendCommand(String command) throws IOException {
        out.println(command);
        return readResponse();
    }

    private String readResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null && !line.equals("###END###")) {
            response.append(line).append("\n");
        }
        return response.toString().trim();
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
            if (out != null) out.close();
            if (in != null) in.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
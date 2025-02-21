package fr.uvsq.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client pour interagir avec le serveur de contrôle à distance.
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final String END_MARKER = "###END###";

    public static void main(String[] args) {
        logger.info("🚀 Starting client...");

        try (Socket socket = new Socket("localhost", 5001);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            logger.info("🔗 Connected to server. Enter commands (type 'exit' to quit):");

            while (true) {
                System.out.print("⌨️ > ");
                String command = scanner.nextLine().trim();

                if ("exit".equalsIgnoreCase(command)) break;
                if (command.isEmpty()) continue;

                out.println(command);
                System.out.println("📥 Response:\n" + readResponse(in));
            }
        } catch (Exception e) {
            logger.error("❌ Client error: {}", e.getMessage(), e);
        } finally {
            logger.info("🛑 Client stopped");
        }
    }

    private static String readResponse(BufferedReader in) throws Exception {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null && !line.equals(END_MARKER)) {
            response.append(line).append("\n");
        }
        return response.toString().trim();
    }
}
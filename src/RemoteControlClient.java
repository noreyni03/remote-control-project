import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;

public class RemoteControlClient {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 5001;
    private static final String END_OF_RESPONSE_MARKER = "%%%END_OF_RESPONSE%%%";

    public static void main(String[] args) {
        System.out.println("Starting Remote Control Client...");
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to server. Enter commands:");

            String command;
            while (true) {
                System.out.print("> ");
                command = scanner.nextLine();

                if ("bye".equalsIgnoreCase(command)) {
                    out.println(command);
                    String response = in.readLine();
                    System.out.println("Server: " + response);
                    break;
                }

                out.println(command);
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals(END_OF_RESPONSE_MARKER)) {
                        break;
                    }
                    response.append(line).append("\n");
                }
                System.out.println("Server response:\n" + response.toString().trim());
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
        System.out.println("Client stopped.");
    }
}
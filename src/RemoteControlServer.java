import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;

public class RemoteControlServer {

    private static final int SERVER_PORT = 5001;

    public static void main(String[] args) {
        System.out.println("Starting Remote Control Server...");
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            InetAddress serverAddress = InetAddress.getLocalHost();
            System.out.println("Server listening on " + serverAddress.getHostAddress() + ":" + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from: " + clientSocket.getInetAddress().getHostAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Server stopped.");
    }
}
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.InetAddress;

public class ClientHandler implements Runnable {

    private static final String END_OF_RESPONSE_MARKER = "%%%END_OF_RESPONSE%%%";
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             clientSocket) {

            String clientAddress = clientSocket.getInetAddress().getHostAddress();
            System.out.println("Handling client: " + clientAddress);

            String command;
            while ((command = in.readLine()) != null) {
                System.out.println("Received command: " + command);

                if ("bye".equalsIgnoreCase(command)) {
                    out.println("Server: Goodbye!");
                    out.println(END_OF_RESPONSE_MARKER);
                    break;
                }

                try {
                    Process process = createProcess(command).start();
                    String output = captureProcessOutput(process);
                    out.println(output);
                    out.println(END_OF_RESPONSE_MARKER);
                } catch (IOException | InterruptedException e) {
                    String error = "Error: " + e.getMessage();
                    out.println(error);
                    out.println(END_OF_RESPONSE_MARKER);
                }
            }
            System.out.println("Client disconnected: " + clientAddress);
        } catch (IOException e) {
            System.err.println("Client handling error: " + e.getMessage());
        }
    }

    private ProcessBuilder createProcess(String command) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            return new ProcessBuilder("/bin/sh", "-c", command);
        }
    }

    private String captureProcessOutput(Process process) throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            while ((line = stdoutReader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
            while ((line = stderrReader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }

            int exitCode = process.waitFor();
            if (exitCode != 0 && output.length() == 0) {
                output.append("Command failed with exit code: ").append(exitCode);
            }
        }
        return output.toString();
    }
}
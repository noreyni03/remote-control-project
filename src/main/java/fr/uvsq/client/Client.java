package fr.uvsq.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 5001);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Connected to server. Enter commands ('exit' to quit):");
            
            while (true) {
                System.out.print("> ");
                String command = scanner.nextLine();
                
                if ("exit".equalsIgnoreCase(command)) break;
                
                out.println(command);
                System.out.println(readResponse(in));
            }
        }
    }

    private static String readResponse(BufferedReader in) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null && !line.equals("###END###")) {
            response.append(line).append("\n");
        }
        return response.toString().trim();
    }
}
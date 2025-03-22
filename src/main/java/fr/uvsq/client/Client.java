package fr.uvsq.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * La classe `Client` représente un client capable de se connecter à un serveur,
 * d'envoyer des commandes et de recevoir des réponses.
 */
public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private static final String END_MARKER = "###END###";

    /**
     * Constructeur de la classe `Client`.
     * Tente d'établir une connexion avec un serveur à l'hôte et au port spécifiés.
     *
     * @param host L'adresse IP ou le nom d'hôte du serveur.
     * @param port Le numéro de port sur lequel le serveur écoute.
     * @throws IOException Si une erreur d'entrée/sortie se produit lors de la connexion.
     */
    public Client(String host, int port) throws IOException {
        System.out.println("[Client] Tentative de connexion à " + host + ":" + port);
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            System.out.println("[Client] Connexion réussie !");
        } catch (IOException e) {
            System.err.println("[Client] Erreur de connexion : " + e.getMessage());
            throw new IOException("Impossible de se connecter au serveur à " + host + ":" + port, e);
        }
    }

    /**
     * Envoie les identifiants au serveur pour s'authentifier.
     *
     * @param login Le login de l'utilisateur.
     * @param password Le mot de passe de l'utilisateur.
     * @return true si l'authentification réussit, false sinon.
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     */
    public boolean authenticate(String login, String password) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Connexion au serveur perdue.");
        }

        // Envoie le login et le mot de passe au serveur
        System.out.println("[Client] Envoi des identifiants : " + login);
        out.println("AUTH"); // Indique au serveur qu'on envoie une authentification
        out.println(login);  // Envoie le login
        out.println(password); // Envoie le mot de passe

        // Lit la réponse du serveur
        String response = in.readLine();
        System.out.println("[Client] Réponse d'authentification : " + response);
        return "OK".equals(response); // Si le serveur répond "OK", c'est réussi
    }

    /**
     * Envoie une commande au serveur et attend une réponse.
     *
     * @param command La commande à envoyer au serveur.
     * @return La réponse du serveur à la commande.
     * @throws IOException Si une erreur d'entrée/sortie se produit lors de l'envoi ou de la réception.
     */
    public String sendCommand(String command) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Connexion au serveur perdue.");
        }

        System.out.println("[Client] Envoi de la commande : " + command);
        out.println(command);

        StringBuilder response = new StringBuilder();
        String line;
        // Lit la réponse du serveur ligne par ligne jusqu'à recevoir le marqueur de fin.
        while ((line = in.readLine()) != null) {
            if (line.equals(END_MARKER)) {
                break;
            }
            response.append(line).append("\n");
        }
        System.out.println("[Client] Réponse reçue : " + response.toString().trim());
        return response.toString().trim();
    }


    public void uploadFile(String filePath) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Connexion au serveur perdue.");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Le fichier n'existe pas : " + filePath);
        }

        // Envoie la commande UPLOAD et le nom du fichier
        out.println("UPLOAD");
        out.println(file.getName());
        out.println(file.length()); // Envoie la taille du fichier en octets

        // Envoie le contenu du fichier
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                socket.getOutputStream().write(buffer, 0, bytesRead);
            }
            socket.getOutputStream().flush();
        }

        // Lit la réponse du serveur
        String response = in.readLine();
        System.out.println("[Client] Réponse du serveur après upload : " + response);
    }


    public String downloadFile(String fileName, String savePath) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Connexion au serveur perdue.");
        }

        // Envoie la commande DOWNLOAD et le nom du fichier
        out.println("DOWNLOAD");
        out.println(fileName);

        // Lit la taille du fichier envoyée par le serveur
        String sizeStr = in.readLine();
        long fileSize = Long.parseLong(sizeStr);
        if (fileSize == -1) {
            String error = in.readLine();
            return "Erreur : " + error;
        }

        // Reçoit le fichier et l’écrit à l’emplacement spécifié
        try (FileOutputStream fos = new FileOutputStream(savePath)) {
            byte[] buffer = new byte[4096];
            long bytesReceived = 0;
            int bytesRead;
            while (bytesReceived < fileSize && (bytesRead = socket.getInputStream().read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                bytesReceived += bytesRead;
            }
            fos.flush();
        }

        return "Fichier téléchargé avec succès à : " + savePath;
    }


    /**
     * Déconnecte le client du serveur en fermant le socket.
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                System.out.println("[Client] Déconnexion du serveur...");
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("[Client] Erreur lors de la fermeture du socket : " + e.getMessage());
        }
    }
}
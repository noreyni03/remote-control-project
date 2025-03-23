package fr.uvsq.client;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * La classe `Client` représente un client pour le système de contrôle à distance.
 * Elle permet de se connecter à un serveur, de s'authentifier, d'envoyer des commandes,
 * de télécharger et d'uploader des fichiers, et de se déconnecter.
 */
public class Client {
    private SSLSocket socket; // Socket SSL pour la communication sécurisée avec le serveur
    private PrintWriter out; // Flux de sortie pour envoyer des données au serveur
    private BufferedReader in; // Flux d'entrée pour recevoir des données du serveur
    private static final String END_MARKER = "###END###"; // Marqueur de fin de réponse du serveur

    /**
     * Constructeur de la classe `Client`.
     * ablit une connexion sécurisée (SSL) avec le serveur spécifié.
     *
     * @param host L'adresse IP ou le nom d'hôte du serveur.
     * @param port Le port sur lequel le serveur écoute.
     * @throws IOException Si une erreur d'entrée/sortie se produit lors de la connexion.
     */
    public Client(String host, int port) throws IOException {
        System.out.println("[Client] Tentative de connexion à " + host + ":" + port);
        try {
            // Configuration du truststore pour la connexion SSL
            System.setProperty("javax.net.ssl.trustStore", "server_keystore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "password");

            // Création d'une socket SSL
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) factory.createSocket(host, port);
            // Initialisation des flux d'entrée et de sortie
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            System.out.println("[Client] Connexion SSL réussie !");
        } catch (IOException e) {
            System.err.println("[Client] Erreur de connexion : " + e.getMessage());
            throw new IOException("Impossible de se connecter au serveur à " + host + ":" + port, e);
        }
    }


    /**
     * Authentifie le client auprès du serveur.
     *
     * @param login    Le nom d'utilisateur (login).
     * @param password Le mot de passe.
     * @return `true` si l'authentification réussit, `false` sinon.
     * @throws IOException Si une erreur d'entrée/sortie se produit ou si la connexion est perdue.
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
        if ("OK".equals(response)) {
            return true;
        } else if ("ERROR".equals(response)) {
            String error = in.readLine();
            throw new IOException("Erreur d'authentification : " + error);
        } else {
            throw new IOException("Réponse inattendue du serveur : " + response);
        }
    }


    /**
     * Envoie une commande au serveur et retourne sa réponse.
     *
     * @param command La commande à envoyer.
     * @return La réponse du serveur.
     * @throws IOException Si une erreur d'entrée/sortie se produit ou si la connexion est perdue.
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
            if (line.equals("ERROR")) {
                String error = in.readLine();
                throw new IOException("Erreur du serveur : " + error);
            }
            response.append(line).append("\n");
        }
        System.out.println("[Client] Réponse reçue : " + response.toString().trim());
        return response.toString().trim();
    }


    /**
     * Envoie un fichier au serveur.
     *
     * @param filePath Le chemin du fichier hrows IOException Si une erreur d'entrée/sortie se produit, si le fichier n'existe pas, ou si la connexion est perdue.
     */
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
        if ("ERROR".equals(response)) {
            String error = in.readLine();
            throw new IOException("Erreur du serveur : " + error);
        }
    }


    /**
     * TvePath Le chemin où sauvegarder le fichier téléchargé.
     * @return Un message indiquant le succès ou l'échec du téléchargement.
     * @throws IOException Si une erreur d'entrée/sortie se produit ou si la connexion est perdue.
     */
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
            throw new IOException("Erreur du serveur : " + error);
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
package fr.uvsq.server.gui;

import fr.uvsq.server.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * La classe `ServerGUI` représente l'interface graphique du serveur pour le système de contrôle à distance.
 * Elle permet de démarrer et d'arrêter le serveur, d'afficher les logs du serveur et de visualiser la liste des clients connectés.
 */
public class ServerGUI extends Application {
    private TextArea logArea;
    private ListView<String> clientList;
    private ObservableList<String> clients = FXCollections.observableArrayList();
    private Server server;
    private boolean isRunning = false;
    private static final Logger logger = LoggerFactory.getLogger(ServerGUI.class);

    /**
     * Méthode principale pour démarrer l'application JavaFX.
     *
     * @param primaryStage La fenêtre principale de l'application.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Remote Control Server - v1.0");
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        // Création de l'en-tête
        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        Label title = new Label("Remote Control Server");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");
        Button toggleBtn = new Button("Start Server");
        toggleBtn.getStyleClass().add("action-btn");
        toggleBtn.setOnAction(e -> toggleServer(toggleBtn));
        header.getChildren().addAll(title, toggleBtn);
        root.setTop(header);

        // Création de la zone d'affichage des clients connectés
        VBox clientBox = new VBox(10);
        clientBox.setPadding(new Insets(10));
        clientBox.setMinWidth(250);
        clientList = new ListView<>(clients);
        clientList.setPlaceholder(new Label("No clients connected"));
        clientBox.getChildren().addAll(new Label("Connected Clients"), clientList);

        // Création de la zone d'affichage des logs
        VBox logBox = new VBox(10);
        logBox.setPadding(new Insets(10));
        logArea = new TextArea();
        logArea.setEditable(false);
        logBox.getChildren().addAll(new Label("Server Logs"), logArea);

        // Ajout des zones clients et logs au centre de la fenêtre
        root.setCenter(new HBox(clientBox, logBox));

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    /**
     * Gère l'action de démarrage/arrêt du serveur.
     *
     * @param btn Le bouton "Start Server/Stop Server" qui a été cliqué.
     */
    private void toggleServer(Button btn) {
        if (!isRunning) {
            // Démarrage du serveur dans un thread séparé
            new Thread(() -> {
                server = new Server();
                // Configuration du callback pour l'affichage des logs
                server.setLogCallback(message ->
                        Platform.runLater(() -> logArea.appendText(message + "\n"))
                );
                // Configuration du callback pour la connexion d'un nouveau client
                server.setClientCallback(client ->
                        Platform.runLater(() -> {
                            if (!clients.contains(client)) {
                                clients.add(client);
                            }
                        })
                );
                // Configuration du callback pour la déconnexion d'un client
                server.setDisconnectCallback(client ->
                        Platform.runLater(() -> {
                            clients.remove(client);
                        })
                );
                server.start();
            }).start();
            btn.setText("Stop Server");
            logArea.appendText("Server started on port 5001\n");
        } else {
            // Arrêt du serveur
            if (server != null) {
                server.stop();
                // Mise à jour de la liste des clients connectés
                clients.clear();
                clients.addAll(server.getConnectedClients());
            }
            btn.setText("Start Server");
            logArea.appendText("Server stopped\n");
        }
        isRunning = !isRunning;
    }

    /**
     * Méthode principale pour lancer l'application.
     *
     * @param args Les arguments de la ligne de commande.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
package fr.uvsq.client.gui;

import fr.uvsq.client.Client;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * La classe `ClientGUI` représente l'interface graphique du client pour le système de contrôle à distance.
 * Elle permet à l'utilisateur de se connecter à un serveur, d'exécuter des commandes et de voir l'historique et la sortie des commandes.
 */
public class ClientGUI extends Application {
    private TextArea outputArea;
    private TextField commandField;
    private ListView<String> historyList;
    private Client client;
    private boolean isConnected = false;
    private String serverIP = "127.0.0.1"; // Modifier si besoin (ex. IP WSL)
    private TextField loginField; // Champ pour le login
    private TextField passwordField; // Champ pour le mot de passe

    /**
     * Méthode principale pour démarrer l'application JavaFX.
     *
     * @param primaryStage La fenêtre principale de l'application.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Remote Control Pro - v1.0");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Crée l'en-tête de l'application.
     * L'en-tête contient le titre de l'application, les champs login/mot de passe et un bouton pour se connecter/déconnecter du serveur.
     *
     * @return Un objet HBox représentant l'en-tête.
     */
    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #2D2D2D;");

        Label title = new Label("Remote Control Pro");
        title.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Ajout des champs login et mot de passe
        Label loginLabel = new Label("Login:");
        loginLabel.setStyle("-fx-text-fill: #FFFFFF;");
        loginField = new TextField();
        loginField.setPromptText("Entrez votre login");
        loginField.setPrefWidth(120);

        Label passwordLabel = new Label("Mot de passe:");
        passwordLabel.setStyle("-fx-text-fill: #FFFFFF;");
        passwordField = new TextField();
        passwordField.setPromptText("Entrez votre mot de passe");
        passwordField.setPrefWidth(120);

        ToggleButton connectionBtn = new ToggleButton("Connect");
        connectionBtn.getStyleClass().add("action-btn");
        connectionBtn.setOnAction(e -> toggleConnection(connectionBtn));

        header.getChildren().addAll(title, loginLabel, loginField, passwordLabel, passwordField, connectionBtn);
        return header;
    }

    /**
     * Crée le contenu principal de l'application.
     * Le contenu principal est divisé en deux parties : l'historique des commandes et la zone de sortie.
     *
     * @return Un objet SplitPane contenant l'historique et la zone de sortie.
     */
    private SplitPane createMainContent() {
        SplitPane splitPane = new SplitPane();

        // Historique des commandes
        VBox historyPane = new VBox(10);
        historyPane.setPadding(new Insets(10));
        historyPane.setMinWidth(250);

        Label historyLabel = new Label("Command History");
        historyList = new ListView<>();
        historyList.setPlaceholder(new Label("No commands executed yet"));
        VBox.setVgrow(historyList, Priority.ALWAYS);
        historyPane.getChildren().addAll(historyLabel, historyList);

        // Zone de sortie
        VBox outputPane = new VBox(10);
        outputPane.setPadding(new Insets(10));
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        VBox.setVgrow(outputArea, Priority.ALWAYS);
        outputPane.getChildren().add(outputArea);

        // Ajout des deux panneaux dans le SplitPane
        splitPane.getItems().addAll(historyPane, outputPane);
        return splitPane;
    }

    /**
     * Crée le pied de page de l'application.
     * Le pied de page contient un champ de texte pour entrer des commandes, un bouton pour exécuter la commande et un bouton pour effacer la sortie.
     *
     * @return Un objet HBox représentant le pied de page.
     */
    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(10));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-background-color: #3A3A3A;");

        commandField = new TextField();
        commandField.setPromptText("Enter system command...");
        commandField.setPrefWidth(400);
        HBox.setHgrow(commandField, Priority.ALWAYS);

        Button sendBtn = new Button("Execute");
        sendBtn.getStyleClass().add("action-btn");
        sendBtn.setOnAction(e -> executeCommand());

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("secondary-btn");
        clearBtn.setOnAction(e -> outputArea.clear());

        footer.getChildren().addAll(commandField, sendBtn, clearBtn);
        return footer;
    }

    /**
     * Gère l'action de connexion/déconnexion du serveur.
     *
     * @param btn Le bouton "Connecter/Déconnecter" qui a été cliqué.
     */
    private void toggleConnection(ToggleButton btn) {
        if (btn.isSelected()) {
            try {
                System.out.println("[ClientGUI] Tentative de connexion à " + serverIP + ":5001");
                client = new Client(serverIP, 5001);
                // Authentification avec le login et mot de passe entrés
                String login = loginField.getText().trim();
                String password = passwordField.getText().trim();
                if (client.authenticate(login, password)) {
                    isConnected = true;
                    btn.setText("Disconnect");
                    outputArea.appendText("✅ Connecté au serveur\n");
                } else {
                    client.disconnect();
                    showErrorDialog("Erreur d'authentification", "Login ou mot de passe incorrect.");
                    btn.setSelected(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Erreur de connexion", e.getMessage());
                btn.setSelected(false);
            }
        } else {
            if (client != null) {
                client.disconnect();
            }
            isConnected = false;
            btn.setText("Connect");
            outputArea.appendText("❌ Déconnecté du serveur\n");
        }
    }

    /**
     * Exécute la commande entrée par l'utilisateur.
     * Envoie la commande au serveur et affiche la réponse dans la zone de sortie.
     */
    private void executeCommand() {
        if (!isConnected) {
            showErrorDialog("Erreur", "Pas connecté au serveur !");
            return;
        }
        String command = commandField.getText().trim();
        if (command.isEmpty()) return;
        try {
            System.out.println("[ClientGUI] Envoi de la commande : " + command);
            String response = client.sendCommand(command);
            historyList.getItems().add(command);
            outputArea.appendText("$ " + command + "\n" + response + "\n\n");
            commandField.clear();
        } catch (Exception e) {
            showErrorDialog("Erreur d'exécution", e.getMessage());
        }
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     *
     * @param title   Le titre de la boîte de dialogue.
     * @param message Le message d'erreur à afficher.
     */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
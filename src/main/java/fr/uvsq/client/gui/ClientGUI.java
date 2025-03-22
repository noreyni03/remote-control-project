package fr.uvsq.client.gui;

import fr.uvsq.client.Client;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * La classe `ClientGUI` représente l'interface graphique du client pour le système de contrôle à distance.
 * Elle permet à l'utilisateur de se connecter à un serveur, d'exécuter des commandes et de gérer des fichiers.
 */
public class ClientGUI extends Application {
    private TextArea outputArea;  // Conserver TextArea
    private TextField commandField;
    private ListView<String> historyList;
    private Client client;
    private boolean isConnected = false;
    private String serverIP = "127.0.0.1";
    private TextField loginField;
    private PasswordField passwordField;
    private TextField passwordTextField;
    private boolean isPasswordVisible = false;
    private Button togglePasswordBtn;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Remote Control Pro - v1.0");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(722);

        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(20));
        header.getStyleClass().add("header");

        HBox titleBox = new HBox(10);
        Label title = new Label("Remote Control Pro");
        title.getStyleClass().add("title");
        Circle statusIndicator = new Circle(8, Color.RED);
        titleBox.getChildren().addAll(title, statusIndicator);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        GridPane connectionGrid = new GridPane();
        connectionGrid.setHgap(15);
        connectionGrid.setVgap(10);

        Label loginLabel = new Label("Login:");
        loginLabel.getStyleClass().add("label");
        loginField = new TextField();
        loginField.setPromptText("Entrez votre login");
        loginField.getStyleClass().add("input-field");

        Label passwordLabel = new Label("Mot de passe:");
        passwordLabel.getStyleClass().add("label");

        passwordField = new PasswordField();
        passwordField.setPromptText("Entrez votre mot de passe");
        passwordField.getStyleClass().add("input-field");
        passwordTextField = new TextField();
        passwordTextField.setPromptText("Entrez votre mot de passe");
        passwordTextField.getStyleClass().add("input-field");
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);

        togglePasswordBtn = new Button("👁️");
        togglePasswordBtn.getStyleClass().add("toggle-btn");
        togglePasswordBtn.setOnAction(e -> togglePasswordVisibility());

        StackPane passwordContainer = new StackPane();
        passwordContainer.getChildren().addAll(passwordField, passwordTextField, togglePasswordBtn);
        StackPane.setAlignment(togglePasswordBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(togglePasswordBtn, new Insets(0, 10, 0, 0));
        StackPane.setMargin(passwordField, new Insets(0, 35, 0, 0));
        StackPane.setMargin(passwordTextField, new Insets(0, 35, 0, 0));

        ToggleButton connectionBtn = new ToggleButton("Connecter");
        connectionBtn.getStyleClass().add("connect-btn");
        connectionBtn.setOnAction(e -> {
            animateButton(connectionBtn);
            toggleConnection(connectionBtn, statusIndicator);
        });

        connectionGrid.add(loginLabel, 0, 0);
        connectionGrid.add(loginField, 1, 0);
        connectionGrid.add(passwordLabel, 0, 1);
        connectionGrid.add(passwordContainer, 1, 1);
        connectionGrid.add(connectionBtn, 2, 0, 1, 2);

        header.getChildren().addAll(titleBox, connectionGrid);
        return header;
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordBtn.setText("👁️");
            isPasswordVisible = false;
        } else {
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            togglePasswordBtn.setText("👁️‍🗨️");
            isPasswordVisible = true;
        }
    }

    private SplitPane createMainContent() {
        SplitPane splitPane = new SplitPane();
        splitPane.getStyleClass().add("split-pane");

        VBox historyPane = new VBox(10);
        historyPane.setPadding(new Insets(15));
        historyPane.setMinWidth(250);

        Label historyLabel = new Label("Command History");
        historyLabel.getStyleClass().add("label");
        historyList = new ListView<>();
        historyList.setPlaceholder(new Label("No commands executed yet"));
        historyList.getStyleClass().add("history-list");
        historyList.setPrefHeight(315);
        VBox.setVgrow(historyList, Priority.ALWAYS);
        historyPane.getChildren().addAll(historyLabel, historyList);

        VBox outputPane = new VBox(10);
        outputPane.setPadding(new Insets(15));
        Label resultLabel = new Label("Result");
        resultLabel.getStyleClass().add("label");
        outputArea = new TextArea();  // Conserver TextArea
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.getStyleClass().add("output-area");
        outputArea.setPrefHeight(315);
        VBox.setVgrow(outputArea, Priority.ALWAYS);
        outputPane.getChildren().addAll(resultLabel, outputArea);

        splitPane.getItems().addAll(historyPane, outputPane);
        return splitPane;
    }

    private VBox createFooter() {
        VBox footer = new VBox(15);
        footer.setPadding(new Insets(20));
        footer.setAlignment(Pos.CENTER);
        footer.getStyleClass().add("footer");

        commandField = new TextField();
        commandField.setPromptText("Enter system command...");
        commandField.setMaxWidth(500);
        commandField.getStyleClass().add("command-field");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button sendBtn = new Button("Execute \uD83D\uDE80");  // Icône de fusée pour "Execute"
        sendBtn.getStyleClass().add("execute-btn");
        sendBtn.setTooltip(new Tooltip("Execute the entered command"));
        sendBtn.setOnAction(e -> {
            animateButton(sendBtn);
            executeCommand();
        });

        Button uploadBtn = new Button("Upload \uD83D\uDCE4");  // Icône d'upload
        uploadBtn.getStyleClass().add("upload-btn");
        uploadBtn.setTooltip(new Tooltip("Upload a file to the server"));
        uploadBtn.setOnAction(e -> {
            animateButton(uploadBtn);
            uploadFile();
        });

        Button downloadBtn = new Button("Download \uD83D\uDCE5");  // Icône de download
        downloadBtn.getStyleClass().add("download-btn");
        downloadBtn.setTooltip(new Tooltip("Download a file from the server"));
        downloadBtn.setOnAction(e -> {
            animateButton(downloadBtn);
            downloadFile();
        });

        Button clearBtn = new Button("Clear \uD83D\uDDD1️");  // Icône de poubelle pour "Clear"
        clearBtn.getStyleClass().add("clear-btn");
        clearBtn.setTooltip(new Tooltip("Clear the output area"));
        clearBtn.setOnAction(e -> {
            animateButton(clearBtn);
            outputArea.clear();
        });

        buttonBox.getChildren().addAll(sendBtn, uploadBtn, downloadBtn, clearBtn);
        footer.getChildren().addAll(commandField, buttonBox);
        return footer;
    }

    private void animateButton(ButtonBase btn) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), btn);
        scale.setToX(1.1);
        scale.setToY(1.1);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
    }

    private void toggleConnection(ToggleButton btn, Circle statusIndicator) {
        if (btn.isSelected()) {
            try {
                System.out.println("[ClientGUI] Tentative de connexion à " + serverIP + ":5001");
                client = new Client(serverIP, 5001);
                String login = loginField.getText().trim();
                String password = isPasswordVisible ? passwordTextField.getText().trim() : passwordField.getText().trim();
                if (client.authenticate(login, password)) {
                    isConnected = true;
                    btn.setText("Déconnecter");
                    btn.getStyleClass().remove("connect-btn");
                    btn.getStyleClass().add("disconnect-btn");
                    outputArea.appendText("✅ Connecté au serveur\n");
                    statusIndicator.setFill(Color.GREEN);
                } else {
                    client.disconnect();
                    showErrorDialog("Erreur d'authentification", "Login ou mot de passe incorrect.");
                    btn.setSelected(false);
                    statusIndicator.setFill(Color.RED);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Erreur de connexion", e.getMessage());
                btn.setSelected(false);
                statusIndicator.setFill(Color.RED);
            }
        } else {
            if (client != null) {
                client.disconnect();
            }
            isConnected = false;
            btn.setText("Connecter");
            btn.getStyleClass().remove("disconnect-btn");
            btn.getStyleClass().add("connect-btn");
            outputArea.appendText("❌ Déconnecté du serveur\n");
            statusIndicator.setFill(Color.RED);
        }
    }

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
            outputArea.appendText("**$ " + command + "**\n" + response + "\n\n");
            commandField.clear();
        } catch (Exception e) {
            showErrorDialog("Erreur d'exécution", e.getMessage());
        }
    }

    private void uploadFile() {
        if (!isConnected) {
            showErrorDialog("Erreur", "Pas connecté au serveur !");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier à envoyer");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                client.uploadFile(file.getAbsolutePath());
                outputArea.appendText("✅ Fichier envoyé : " + file.getName() + "\n");
            } catch (IOException e) {
                showErrorDialog("Erreur d’upload", e.getMessage());
            }
        }
    }

    private void downloadFile() {
        if (!isConnected) {
            showErrorDialog("Erreur", "Pas connecté au serveur !");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Télécharger un fichier");
        dialog.setHeaderText("Entrez le nom du fichier à télécharger");
        dialog.setContentText("Nom du fichier :");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String fileName = result.get().trim();
            if (fileName.isEmpty()) {
                showErrorDialog("Erreur", "Le nom du fichier ne peut pas être vide !");
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir où sauvegarder le fichier");
            fileChooser.setInitialFileName(fileName);
            File saveFile = fileChooser.showSaveDialog(null);
            if (saveFile != null) {
                try {
                    String response = client.downloadFile(fileName, saveFile.getAbsolutePath());
                    outputArea.appendText("📥 " + response + "\n");
                } catch (IOException e) {
                    showErrorDialog("Erreur de téléchargement", e.getMessage());
                }
            }
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
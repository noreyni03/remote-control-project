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
import javafx.scene.control.SplitPane;  // Correction de l'import
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class ClientGUI extends Application {
    private TextArea outputArea;
    private TextField commandField;
    private ListView<String> historyList;
    private Client client;
    private boolean isConnected = false;

    @Override
    public void start(Stage primaryStage) {
        // Configuration de la fenêtre principale
        primaryStage.setTitle("Remote Control Pro - v1.0");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Layout principal
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Corps principal
        SplitPane mainContent = createMainContent();
        root.setCenter(mainContent);

        // Footer
        HBox footer = createFooter();
        root.setBottom(footer);

        // Configuration de la scène
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #2D2D2D;");

        Label title = new Label("Remote Control Pro");
        title.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 20px; -fx-font-weight: bold;");

        ToggleButton connectionBtn = new ToggleButton("Connect");
        connectionBtn.getStyleClass().add("action-btn");
        connectionBtn.setOnAction(e -> toggleConnection(connectionBtn));

        header.getChildren().addAll(title, connectionBtn);
        return header;
    }

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

        splitPane.getItems().addAll(historyPane, outputPane);
        return splitPane;
    }

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

    private void toggleConnection(ToggleButton btn) {
        if (btn.isSelected()) {
            try {
                client = new Client("localhost", 5001);
                isConnected = true;
                btn.setText("Disconnect");
                outputArea.appendText("Connected to server\n");
            } catch (Exception e) {
                showErrorDialog("Connection Error", e.getMessage());
                btn.setSelected(false);
            }
        } else {
            if (client != null) {
                client.disconnect();
            }
            isConnected = false;
            btn.setText("Connect");
            outputArea.appendText("Disconnected from server\n");
        }
    }

    private void executeCommand() {
        if (!isConnected) {
            showErrorDialog("Connection Error", "Not connected to server!");
            return;
        }

        String command = commandField.getText().trim();
        if (command.isEmpty()) return;

        try {
            String response = client.sendCommand(command);
            historyList.getItems().add(command);
            outputArea.appendText("$ " + command + "\n" + response + "\n\n");
            commandField.clear();
        } catch (Exception e) {
            showErrorDialog("Execution Error", e.getMessage());
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
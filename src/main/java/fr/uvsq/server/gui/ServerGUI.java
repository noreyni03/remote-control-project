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

public class ServerGUI extends Application {
    private TextArea logArea;
    private ListView<String> clientList;
    private ObservableList<String> clients = FXCollections.observableArrayList();
    private Server server;
    private boolean isRunning = false;
    private static final Logger logger = LoggerFactory.getLogger(ServerGUI.class);

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Remote Control Server - v1.0");
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        Label title = new Label("Remote Control Server");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");
        Button toggleBtn = new Button("Start Server");
        toggleBtn.getStyleClass().add("action-btn");
        toggleBtn.setOnAction(e -> toggleServer(toggleBtn));
        header.getChildren().addAll(title, toggleBtn);
        root.setTop(header);

        VBox clientBox = new VBox(10);
        clientBox.setPadding(new Insets(10));
        clientBox.setMinWidth(250);
        clientList = new ListView<>(clients);
        clientList.setPlaceholder(new Label("No clients connected"));
        clientBox.getChildren().addAll(new Label("Connected Clients"), clientList);

        VBox logBox = new VBox(10);
        logBox.setPadding(new Insets(10));
        logArea = new TextArea();
        logArea.setEditable(false);
        logBox.getChildren().addAll(new Label("Server Logs"), logArea);

        root.setCenter(new HBox(clientBox, logBox));

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    private void toggleServer(Button btn) {
        if (!isRunning) {
            new Thread(() -> {
                server = new Server();
                server.setLogCallback(message ->
                        Platform.runLater(() -> logArea.appendText(message + "\n"))
                );
                server.setClientCallback(client ->
                        Platform.runLater(() -> {
                            if (!clients.contains(client)) {
                                clients.add(client);
                            }
                        })
                );
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
            if (server != null) {
                server.stop();
                clients.clear();
                clients.addAll(server.getConnectedClients());
            }
            btn.setText("Start Server");
            logArea.appendText("Server stopped\n");
        }
        isRunning = !isRunning;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
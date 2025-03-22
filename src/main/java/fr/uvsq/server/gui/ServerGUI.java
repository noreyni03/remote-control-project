package fr.uvsq.server.gui;

import fr.uvsq.server.Server;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * La classe `ServerGUI` représente l'interface graphique du serveur pour le système de contrôle à distance.
 * Elle permet de démarrer/arrêter le serveur, d'afficher les clients connectés et les logs d'activité.
 */
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

        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        header.getStyleClass().add("header");

        Label title = new Label("Remote Control Server");
        title.getStyleClass().add("title");

        ToggleButton toggleBtn = new ToggleButton("Start Server ✔");  // Icône de cercle vert pour "Start"
        toggleBtn.getStyleClass().add("connect-btn");
        toggleBtn.setOnAction(e -> {
            animateButton(toggleBtn);
            toggleServer(toggleBtn);
        });

        header.getChildren().addAll(title, toggleBtn);
        return header;
    }

    private HBox createMainContent() {
        HBox mainContent = new HBox(10);
        mainContent.setPadding(new Insets(10));

        VBox clientBox = new VBox(10);
        clientBox.setPadding(new Insets(10));
        clientBox.setMinWidth(250);
        Label clientLabel = new Label("Connected Clients");
        clientLabel.getStyleClass().add("label");
        clientList = new ListView<>(clients);
        clientList.setPlaceholder(new Label("No clients connected"));
        clientList.getStyleClass().add("history-list");
        clientBox.getChildren().addAll(clientLabel, clientList);
        VBox.setVgrow(clientList, Priority.ALWAYS);

        VBox logBox = new VBox(10);
        logBox.setPadding(new Insets(10));
        Label logLabel = new Label("Server Logs");
        logLabel.getStyleClass().add("label");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.getStyleClass().add("output-area");
        logBox.getChildren().addAll(logLabel, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        mainContent.getChildren().addAll(clientBox, logBox);
        HBox.setHgrow(logBox, Priority.ALWAYS);
        return mainContent;
    }

    private VBox createFooter() {
        VBox footer = new VBox(15);
        footer.setPadding(new Insets(20));
        footer.setAlignment(Pos.CENTER);
        footer.getStyleClass().add("footer");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button clearBtn = new Button("Clear ️");
        clearBtn.getStyleClass().add("clear-btn");
        clearBtn.setTooltip(new Tooltip("Clear the server logs"));
        clearBtn.setOnAction(e -> {
            animateButton(clearBtn);
            logArea.clear();
        });

        buttonBox.getChildren().add(clearBtn);
        footer.getChildren().add(buttonBox);
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

    private void toggleServer(ToggleButton btn) {
        if (!isRunning) {
            new Thread(() -> {
                server = new Server();
                server.setLogCallback(message ->
                        Platform.runLater(() -> {
                            if (message.startsWith("Commande exécutée : ")) {
                                String commandPart = message.substring(0, message.indexOf("\n"));
                                String resultPart = message.substring(message.indexOf("\n") + 1);
                                logArea.appendText("[$ " + commandPart + "]\n" + resultPart + "\n");
                            } else {
                                logArea.appendText(message + "\n");
                            }
                        })
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
            btn.setText("Stop Server ❌");  // Icône de cercle rouge pour "Stop"
            btn.getStyleClass().remove("connect-btn");
            btn.getStyleClass().add("disconnect-btn");
            logArea.appendText("✅ Server started on port 5001\n");
        } else {
            if (server != null) {
                server.stop();
                clients.clear();
                clients.addAll(server.getConnectedClients());
            }
            btn.setText("Start Server ✔");
            btn.getStyleClass().remove("disconnect-btn");
            btn.getStyleClass().add("connect-btn");
            logArea.appendText("❌ Server stopped\n");
        }
        isRunning = !isRunning;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
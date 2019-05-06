package view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import controller.ServerController;

import java.util.List;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 */

/**
 * Server Gui
 */

public class ServerFrame extends Application {

    private ServerController controller = new ServerController(this);

    private TextArea log = new TextArea();
    private Button startButton = new Button("Start Server");
    private Button stopButton = new Button("Stop Server");
    private Button disconnectButton = new Button("Disconnect Chosen Client");
    private TextField port = new TextField("" + ServerController.defaultPort);
    private TextField maxClients = new TextField("" + ServerController.maxNumClients);

    private ListView<String> clientsListView = new ListView<>(FXCollections.observableArrayList());
    private ListView<String> clientsStatusView = new ListView<>(FXCollections.observableArrayList());

    public void start(Stage stage) {

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 5, 5, 5));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.CENTER);

        gridPane.add(new Text("Port"), 2, 0, 1, 1);
        gridPane.add(port, 3, 0, 1, 1);
        gridPane.add(new Text("Max clients"), 2, 1, 1, 1);
        gridPane.add(maxClients, 3, 1, 1, 1);

        gridPane.add(startButton, 0, 0, 1, 1);
        gridPane.add(stopButton, 0, 1, 1, 1);

        gridPane.add(clientsListView, 0, 2, 2, 1);
        gridPane.add(clientsStatusView, 2, 2, 2, 1);

        gridPane.add(disconnectButton, 0, 3, 2, 1);
        gridPane.add(log, 0, 4, 4, 1);

        gridPane.setPrefSize(500, 600);

        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        RowConstraints row3 = new RowConstraints();
        RowConstraints row4 = new RowConstraints();
        RowConstraints row5 = new RowConstraints();
        row1.setPercentHeight(8);
        row2.setPercentHeight(8);
        row3.setPercentHeight(36);
        row4.setPercentHeight(8);
        row5.setPercentHeight(40);
        gridPane.getRowConstraints().addAll(row1, row2, row3, row4, row5);

        addListeners();

        Scene scene = new Scene(gridPane);
        scene.getStylesheets().add("server.css");
        stage.setTitle("Maze game server");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest((e) -> {
            if (controller != null) {
                controller.disconnectAllClients();
                controller.closeThread();
            }
        });
    }

    /**
     * adds listeners to controls
     */
    private void addListeners() {
        port.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                port.setText(oldValue);
            }
        });
        port.focusedProperty().addListener((ObservableValue<? extends Boolean> arg, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                port.setText("" + controller.checkPort(port.getText()));
            }
        });

        maxClients.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                maxClients.setText(oldValue);
            }
        });

        maxClients.focusedProperty().addListener((ObservableValue<? extends Boolean> arg, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                maxClients.setText("" + controller.checkMaxClients(maxClients.getText()));
            }
        });


        startButton.setOnAction(e -> {
            port.setText("" + controller.checkPort(port.getText()));
            maxClients.setText("" + controller.checkMaxClients(maxClients.getText()));
            if (controller.startServer(port.getText(), maxClients.getText())) {
                startButton.setDisable(true);
                stopButton.setDisable(false);
            }
        });

        stopButton.setDisable(true);
        stopButton.setOnAction(e-> stopServer());

        disconnectButton.setOnAction(e -> {
            String name = clientsListView.getSelectionModel().getSelectedItem();
            if (name != null) {
                controller.disconnectClient(name);
                appendLog(name + " disconnected.");
            } else {
                appendLog("Client not selected.");
            }
        });
    }

    @Override
    public void stop() {
        stopServer();
    }

    /**
     * Appends text log window
     *
     * @param text
     */
    public synchronized void appendLog(String text) {
        Platform.runLater(() -> {
            log.appendText(text);
            log.appendText("\n");
        });
    }

    /**
     * Refreshes clients list
     *
     * @param clients
     * @param statuses
     */
    public synchronized void refreshClients(List<String> clients, List<String> statuses) {
        Platform.runLater(() -> {
            clientsListView.getItems().clear();
            clientsStatusView.getItems().clear();
            clientsListView.getItems().addAll(clients);
            clientsStatusView.getItems().addAll(statuses);
        });
    }

    /**
     * Stops the server
     */
    private void stopServer() {
        startButton.setDisable(false);
        stopButton.setDisable(true);
        if (controller != null) {
            controller.disconnectAllClients();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
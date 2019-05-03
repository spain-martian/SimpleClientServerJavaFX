package server;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;

import message.Message;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 */

public class ServerFrame extends Application {

    private ServerController controller = new ServerController(this);

    private TextArea log = new TextArea();
    private ObservableList<String> clientsList = FXCollections.observableArrayList();
    private Button startButton = new Button("Start");
    private Button stopButton = new Button("Stop");
    private Button sendButton = new Button("Send");


    public void start(Stage stage) {

        TextField port = new TextField("" + ServerController.defaultPort);
        TextField maxClients = new TextField("" + ServerController.maxNumClients);
        TextField textToSend = new TextField("");

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

        ListView<String> clientsListView = new ListView<String>(clientsList);
        clientsListView.setPrefSize(140, 200);

        ListView<Message.Type> types = new ListView<>();
        types.getItems().add(Message.Type.START);
        types.getItems().add(Message.Type.END);
        types.getItems().add(Message.Type.INFORM);
        types.getItems().add(Message.Type.REQUEST);
        types.setPrefSize(140, 80);

        log.setPrefSize(300, 150);

        Button disconnectButton = new Button("Disconnect");
        disconnectButton.setOnAction(e -> {
            String name = clientsListView.getSelectionModel().getSelectedItem();
            if (name != null) {
                controller.disconnectClient(name);
                appendLog(name + " disconnected.");
            } else {
                appendLog("Client not selected.");
            }
        });

        sendButton.setOnAction(e -> {
            String name = clientsListView.getSelectionModel().getSelectedItem();
            if (name != null) {
                Message.Type type = types.getSelectionModel().getSelectedItem();
                if (type != null) {
                    int i = controller.getIndexForClientName(name);
                    try {
                        controller.getClientThreads()[i].sendMessage(
                                new Message(type, textToSend.getText()));
                    } catch (IOException ex) {
                        appendLog("Error sending message.");
                    }
                } else {
                    appendLog("Message type not selected.");
                }
            } else {
                appendLog("Client not selected.");  
            }
        });


        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(0, 0, 10, 0));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setAlignment(Pos.CENTER);

        gridPane.add(new Text("Port"), 0, 0);
        gridPane.add(new Text("Max clients"), 1, 0);

        gridPane.add(port, 0, 1);
        gridPane.add(maxClients, 1, 1);

        gridPane.add(startButton, 0, 2);
        gridPane.add(new Text("Text to send"), 1, 2);

        gridPane.add(stopButton, 0, 3);
        gridPane.add(textToSend, 1, 3);

        gridPane.add(clientsListView, 0, 4);
        gridPane.add(types, 1, 4);

        gridPane.add(disconnectButton, 0, 5);
        gridPane.add(sendButton, 1, 5);

        VBox vBox = new VBox();
        vBox.getChildren().add(gridPane);
        vBox.getChildren().add(log);
        vBox.setPadding(new Insets(10, 10, 10, 10));

        Scene scene = new Scene(vBox);
        scene.getStylesheets().add("stylesheet.css");
        stage.setTitle("Game server");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        controller.disconnectAllClients();
    }

    synchronized void appendLog(String text) {
        log.appendText(text + "\n");
    }

    ObservableList<String> getClientsList() {
        return clientsList;
    }

    private void stopServer() {
        startButton.setDisable(false);
        stopButton.setDisable(true);
        controller.disconnectAllClients();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
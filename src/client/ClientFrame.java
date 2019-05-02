package client;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import message.Message;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 */

public class ClientFrame extends Application {
    private static final int defaultPort = 4433;
    private ClientController controller = new ClientController(this);

    private Button startButton = new Button("Start");
    private Button stopButton = new Button("Stop");
    private Button sendButton = new Button("Send");

    private Button leftButton = new Button("Left");
    private Button upButton = new Button("Up");
    private Button downButton = new Button("Down");
    private Button rightButton = new Button("Right");

    private TextArea log = new TextArea();
    private Text portLabel = new Text("Port");
    private Text nameLabel = new Text("Name");
    private Text comment = new Text("");

    private TextField port = new TextField("" + defaultPort);
    private TextField name = new TextField("Client1");
    private TextField textToSend = new TextField("");

    public void start(Stage stage) {

        ListView<Message.Type> types = new ListView<>();
        types.getItems().add(Message.Type.START);
        types.getItems().add(Message.Type.END);
        types.getItems().add(Message.Type.INFORM);
        types.getItems().add(Message.Type.REQUEST);
        types.setPrefSize(140, 95);

        VBox vBox = new VBox();

        vBox.getChildren().add(portLabel);
        vBox.getChildren().add(port);
        vBox.getChildren().add(nameLabel);
        vBox.getChildren().add(name);

        HBox hBox1 = new HBox();
        hBox1.getChildren().add(startButton);
        hBox1.getChildren().add(stopButton);
        vBox.getChildren().add(hBox1);
        hBox1.setSpacing(5);

        HBox hBox2 = new HBox();
        hBox2.getChildren().add(new Text("Data to send"));
        hBox2.getChildren().add(textToSend);
        hBox2.setSpacing(5);


        vBox.getChildren().add(hBox2);
        vBox.getChildren().add(sendButton);
        vBox.getChildren().add(types);
        vBox.setSpacing(10);

        HBox hBox3 = new HBox();
        hBox3.getChildren().add(leftButton);
        hBox3.getChildren().add(upButton);
        hBox3.getChildren().add(downButton);
        hBox3.getChildren().add(rightButton);
        hBox3.setSpacing(10);
        hBox3.setAlignment(Pos.CENTER);

        VBox vBox2 = new VBox();
        vBox2.getChildren().add(comment);


        Group group = lines();
        vBox2.getChildren().add(group);
        vBox2.getChildren().add(hBox3);
        vBox2.setSpacing(10);

        HBox hBox = new HBox();
        hBox.getChildren().add(vBox);
        hBox.getChildren().add(vBox2);
        hBox.setPadding(new Insets(10, 10, 10, 10));
        hBox.setSpacing(10);

        log.setPrefSize(400, 140);

        VBox vBox3 = new VBox();
        vBox3.getChildren().add(hBox);
        vBox3.getChildren().add(log);
        vBox3.setSpacing(10);

        types.setFocusTraversable(false);
        log.setFocusTraversable(false);
        startButton.setFocusTraversable(false);
        stopButton.setFocusTraversable(false);
        sendButton.setFocusTraversable(false);
        textToSend.setFocusTraversable(false);
        name.setFocusTraversable(false);
        port.setFocusTraversable(false);


        Scene scene = new Scene(vBox3,500, 500);

        port.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{1,6}?")) {
                    port.setText(oldValue);
                }
            }
        });

        startButton.setOnAction(e->{
            if (controller.startClient()) {
                startButton.setDisable(true);
                stopButton.setDisable(false);
                sendButton.setDisable(false);
            }
        });
        stopButton.setDisable(true);
        sendButton.setDisable(true);
        stopButton.setOnAction(e->setClientStopped());
        sendButton.setOnAction(e->{
            Message.Type type = types.getSelectionModel().getSelectedItem();
            if (type != null) {
                controller.sendMessage(new Message(type, textToSend.getText()));
            }
        });

        leftButton.setOnAction(e->controller.sendMessage(new Message(Message.Type.REQUEST, "move left")));
        rightButton.setOnAction(e->controller.sendMessage(new Message(Message.Type.REQUEST, "move right")));
        upButton.setOnAction(e->controller.sendMessage(new Message(Message.Type.REQUEST, "move up")));
        downButton.setOnAction(e->controller.sendMessage(new Message(Message.Type.REQUEST, "move down")));

        leftButton.setDisable(true);
        rightButton.setDisable(true);
        upButton.setDisable(true);
        downButton.setDisable(true);

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (startButton.isDisable()) {
                    switch (event.getCode()) {
                        case UP:
                            controller.sendMessage(new Message(Message.Type.REQUEST, "move up"));
                            break;
                        case DOWN:
                            controller.sendMessage(new Message(Message.Type.REQUEST, "move down"));
                            break;
                        case LEFT:
                            controller.sendMessage(new Message(Message.Type.REQUEST, "move left"));
                            break;
                        case RIGHT:
                            controller.sendMessage(new Message(Message.Type.REQUEST, "move right"));
                            break;
                    }
                    rect.requestFocus();
                }
            }
        });

        stage.setTitle("Game client");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        setClientStopped();
    }

    synchronized void appendLog(String text) {
        log.appendText(text + "\n");
    }

    int getPort() {
        try {
            return Integer.parseInt(port.getText());
        } catch (Exception e) {
            return defaultPort;
        }
    }

    String getName() {
        return name.getText();
    }

    private void setClientStopped() {
        startButton.setDisable(false);
        stopButton.setDisable(true);
        sendButton.setDisable(true);
        controller.cancelClient();
    }

    void informClientStopped() {
        startButton.setDisable(false);
        stopButton.setDisable(true);
        sendButton.setDisable(true);
    }

    void setComment(String text) {
        comment.setText(text);
    }

    private static final int nLines = 11;
    private static final int wRect = 300;
    private static final int hRect = 300;
    private Rectangle rect = new Rectangle(0, 0, wRect, hRect);
    private Line[][] vLines = new Line[nLines][nLines + 1];
    private Line[][] hLines = new Line[nLines + 1][nLines];
    private Circle he = new Circle(wRect / nLines / 3);
    private Color[] colors = {Color.LIGHTGRAY, Color.WHITE, Color.BLACK};

    private Group lines() {
        Group group = new Group();

        rect.setFill(colors[1]);
        group.getChildren().add(rect);

        he.setCenterX((nLines/2 + 0.5) * wRect / nLines);
        he.setCenterY((nLines/2 + 0.5) * hRect / nLines);
        he.setFill(Color.BLUE);
        group.getChildren().add(he);

        for (int r = 0; r < vLines.length; r++) {
            for (int c = 0; c < vLines[r].length; c++) {
                vLines[r][c] = new Line(c * wRect / nLines,
                        r * hRect / nLines,
                        c * wRect / nLines,
                        r * hRect / nLines + hRect / nLines);
                group.getChildren().add(vLines[r][c]);
                vLines[r][c].setStroke(Color.LIGHTGRAY);
            }
        }

        for (int r = 0; r < hLines.length; r++) {
            for (int c = 0; c < hLines[r].length; c++) {
                hLines[r][c] = new Line(c * wRect / nLines,
                        r * hRect / nLines,
                        c * wRect / nLines + wRect / nLines,
                        r * hRect / nLines);
                group.getChildren().add(hLines[r][c]);
                hLines[r][c].setStroke(Color.LIGHTGRAY);
            }
        }
        return group;
    }

    public Line[][] getVLines() {
        return vLines;
    }

    public Line[][] getHLines() {
        return hLines;
    }

    public Color[] getColors() {
        return colors;
    }

    public void setHim(int r, int c) {
        he.setCenterX((c + 0.5) * wRect / nLines);
        he.setCenterY((r + 0.5) * hRect / nLines);
    }

    public void setHisColor(int color) {
        he.setFill(color == 0 ? Color.BLUE : Color.RED);
    }


    public int getnLines() {
        return nLines;
    }

    public static void main(String[] args) {
        launch(args);
    }
}




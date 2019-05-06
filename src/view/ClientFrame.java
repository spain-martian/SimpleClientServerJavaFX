package view;

import controller.ClientController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import model.Message;
import controller.ServerController;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 * Clients part of the game
 */

public class ClientFrame extends Application {
    private ClientController controller = new ClientController(this, nScreenCells, nScreenCells);

    private Button startButton = new Button("Connect");
    private Button stopButton = new Button("Disconnect");

    private Button leftButton = new Button("Left");
    private Button upButton = new Button("Up");
    private Button downButton = new Button("Down");
    private Button rightButton = new Button("Right");

    private TextArea log = new TextArea();
    private Text portLabel = new Text("Port");
    private Text nameLabel = new Text("Name");
    private Label comment = new Label("Use keyboard to move");

    private TextField port = new TextField("" + ServerController.defaultPort);
    private TextField name = new TextField("Player1");

    //------------ Maze ----------
    private static final int nScreenCells = 5;
    private static final int wCell = 33;
    private static final int wRect = nScreenCells * wCell;
    private Rectangle rect = new Rectangle(0, 0, wRect, wRect);
    private Rectangle[][] cells = new Rectangle[nScreenCells][nScreenCells];

    private Line[][] vLines = new Line[nScreenCells][nScreenCells + 1];
    private Line[][] hLines = new Line[nScreenCells + 1][nScreenCells];
    private Circle ball = new Circle(wRect / nScreenCells / 3);
    private Color[] colors = {Color.LIGHTGRAY, Color.WHITE, Color.BLACK};

    private static Color ballBefore = Color.GRAY;
    private static Color ballIn = Color.GREEN;
    private static Color ballOut = Color.RED;

    private static Color wallUnknown = Color.LIGHTGRAY;
    private static Color wallKnown = Color.BLACK;
    private static Color wallNo = Color.WHITE;

    private static Color cellUnknown = Color.WHITESMOKE;
    private static Color cellVisited = Color.WHITE;
    //---------------

    public void start(Stage stage) {
        HBox inputs = new HBox();
        port.setPrefWidth(100);
        inputs.getChildren().addAll(portLabel, port, nameLabel, name);
        inputs.setSpacing(10);
        inputs.setAlignment(Pos.CENTER);

        HBox buttons = new HBox();
        buttons.getChildren().addAll(startButton, stopButton);
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER);

        log.setPrefWidth(wRect);
        log.setMinWidth(400);
        log.setDisable(true);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        Group maze = lines();

        vBox.getChildren().addAll(inputs, buttons, maze, comment, log);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10, 10, 10, 10));

        Scene scene = new Scene(vBox);
        scene.getStylesheets().add("client.css");

        addListeners(scene);

        stage.setTitle("Maze game client");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest((e) -> {
            if (controller != null) {
                controller.stopClient();
            }
        });

        log.setFocusTraversable(false);
        startButton.setFocusTraversable(false);
        stopButton.setFocusTraversable(false);
        name.setFocusTraversable(false);
        port.setFocusTraversable(false);
    }

    /**
     * Adds listeners to controls
     * @param scene
     */
    private void addListeners(Scene scene) {
        port.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                port.setText(oldValue);
            }
        });

        startButton.setOnAction(e -> {
            if (controller.startClient()) {
                startButton.setDisable(true);
                stopButton.setDisable(false);
            }
        });

        stopButton.setDisable(true);
        stopButton.setOnAction(e -> setClientStopped());

        leftButton.setOnAction(e -> controller.sendMessage(new Message(Message.Type.REQUEST, "move left")));
        rightButton.setOnAction(e -> controller.sendMessage(new Message(Message.Type.REQUEST, "move right")));
        upButton.setOnAction(e -> controller.sendMessage(new Message(Message.Type.REQUEST, "move up")));
        downButton.setOnAction(e -> controller.sendMessage(new Message(Message.Type.REQUEST, "move down")));

        leftButton.setDisable(true);
        rightButton.setDisable(true);
        upButton.setDisable(true);
        downButton.setDisable(true);

        scene.setOnKeyPressed((e) -> {
            if (startButton.isDisable()) {
                switch (e.getCode()) {
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
        });
    }

    @Override
    public void stop() {
        setClientStopped();
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
     * Gets port number from text field
     * @return  int value
     */
    public int getPort() {
        try {
            return Integer.parseInt(port.getText());
        } catch (Exception e) {
            return ServerController.defaultPort;
        }
    }

    /**
     * Client's name
     * @return
     */
    public String getName() {
        return name.getText();
    }

    private void setClientStopped() {
        startButton.setDisable(false);
        stopButton.setDisable(true);
        controller.stopClient();
    }

    /**
     * Informs GUI that the connection is lost
     */
    public void informClientStopped() {
        Platform.runLater(() -> {
            appendLog("Client thread stopped");
            startButton.setDisable(false);
            stopButton.setDisable(true);
        });
    }

    /**
     * Sets comments text
     * @param text
     */
    public void setComment(String text) {
        Platform.runLater(() -> {
            comment.setText(text);
        });
    }

    /**
     * Creates Maze picture
     * @return  JavaFX group
     */
    private Group lines() {
        Group group = new Group();

        rect.setFill(colors[1]);
        group.getChildren().add(rect);

        for (int r = 0; r < vLines.length; r++) {
            for (int c = 0; c < vLines[r].length; c++) {
                vLines[r][c] = new Line(c * wRect / nScreenCells,
                        r * wRect / nScreenCells,
                        c * wRect / nScreenCells,
                        r * wRect / nScreenCells + wRect / nScreenCells);
                group.getChildren().add(vLines[r][c]);
                vLines[r][c].setStroke(wallUnknown);
            }
        }

        for (int r = 0; r < hLines.length; r++) {
            for (int c = 0; c < hLines[r].length; c++) {
                hLines[r][c] = new Line(c * wRect / nScreenCells,
                        r * wRect / nScreenCells,
                        c * wRect / nScreenCells + wRect / nScreenCells,
                        r * wRect / nScreenCells);
                group.getChildren().add(hLines[r][c]);
                hLines[r][c].setStroke(wallUnknown);
            }
        }

        for (int r = 0; r < nScreenCells; r++) {
            for (int c = 0; c < nScreenCells; c++) {
                cells[r][c] = new Rectangle(c * wCell + 1, r * wCell + 1, wCell - 2, wCell - 2); //x, y = c, r!
                cells[r][c].setFill(cellUnknown);
                group.getChildren().add(cells[r][c]);
            }
        }

        setBallRC(nScreenCells /2, nScreenCells /2);
        ball.setFill(ballBefore);

        group.getChildren().add(ball);
        return group;
    }

    /**
     * Redraws maze during the game process
     */
    public void redrawMaze() {
        for (int r = 0; r < vLines.length; r++) {
            for (int c = 0; c < vLines[r].length; c++) {
                Boolean exists = controller.getVWall(r, c);
                if (exists == null) vLines[r][c].setStroke(wallUnknown);
                else if (exists) vLines[r][c].setStroke(wallKnown);
                else vLines[r][c].setStroke(wallNo);
            }
        }

        for (int r = 0; r < hLines.length; r++) {
            for (int c = 0; c < hLines[r].length; c++) {
                Boolean exists = controller.getHWall(r, c);
                if (exists == null) hLines[r][c].setStroke(wallUnknown);
                else if (exists) hLines[r][c].setStroke(wallKnown);
                else hLines[r][c].setStroke(wallNo);
            }
        }

        for (int r = 0; r < nScreenCells; r++) {
            for (int c = 0; c < nScreenCells; c++) {
                cells[r][c].setFill(controller.isCellVisited(r, c) ? cellVisited : cellUnknown);
            }
        }

        setBallRC(controller.getCurrentRow(), controller.getCurrentCol());
        ball.setFill(controller.isBallOut() ? ballOut : ballIn);
    }

    /**
     * Sets ball'c coordinates
     * @param r     row
     * @param c     column
     */
    public void setBallRC(int r, int c) {
        ball.setCenterX((c + 0.5) * wRect / nScreenCells);
        ball.setCenterY((r + 0.5) * wRect / nScreenCells);
    }

    public static void main(String[] args) {
        launch(args);
    }
}




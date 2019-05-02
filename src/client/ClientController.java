package client;

import javafx.application.Platform;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.TreeMap;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import message.Message;

/**
 * Created by Public on 20-Aug-18.
 */

public class ClientController {
    private ClientFrame frame;
    private Socket socket;
    private ClientGameDriver gameDriver;
    private static final String hostName = "localhost";
    private ObjectOutputStream requestStream;
    private ObjectInputStream answerStream;

    ClientController(ClientFrame frame) {
        this.frame = frame;
        nLines = frame.getnLines();
    }

    boolean startClient() {
        int port = frame.getPort();
        gameDriver = new ClientGameDriver(this);
        setHisColor(0);
        dRow = -nLines / 2;
        dCol = -nLines / 2;
        setColors(0, 0, null, null);

        InetAddress ina = null;
        try {
            ina = InetAddress.getByName(hostName);
            try {
                socket = new Socket(ina, port);
                try {
                    requestStream = new ObjectOutputStream(socket.getOutputStream());
                    requestStream.flush();
                    answerStream = new ObjectInputStream(socket.getInputStream());

                    sendMessage(new Message(Message.Type.START, frame.getName()));
                    appendLog("client: start message sent, waiting for answer.");

                    Message answer = (Message) answerStream.readObject();
                    if (answer.getType() == Message.Type.ANSWER) {
                        appendLog("answer: " + answer);
                    } else {
                        if (answer.getType() == Message.Type.END) {
                            appendLog("Server disconnected.");
                            return false;
                        } else {
                            appendLog("Client: unexpected message: " + answer);
                        }
                    }
                } catch (Exception e) {
                    appendLog("IO error " + e);
                    cancelClient();
                    return false;
                }
            } catch (IOException ex) {
                appendLog("Cannot connect to the host");
                return false;
            }
        } catch (UnknownHostException u) {
            appendLog("Cannot find host name");
            return false;
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean end = false;
                    while (!end) {
                        appendLog("Waiting for input.");
                        Message answer = (Message) answerStream.readObject();
                        appendLog("message received: " + answer);
                        if (answer.getType() == Message.Type.END) {
                            end = true;
                        }
                        if (answer.getType() == Message.Type.ANSWER) {
                            gameDriver.react(answer);
                        }
                    }
                } catch (Exception ex) {
                    appendLog("Error reading socket.");
                    cancelClient();
                    frame.informClientStopped();
                    //ex.printStackTrace();
                }
                cancelClient();
            }
        }).start();
        return true;
    }

    void sendMessage(Message message) {

        try {
            requestStream.writeObject(message);
            requestStream.flush();
            requestStream.reset();
            appendLog(message + " sent.");
        } catch (IOException e) {
            appendLog("Error sending message " + e);
        }
    }

    void appendLog(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                frame.appendLog(text);
            }
        });
    }

    void cancelClient() {
        informClientStopped();
        try {
            answerStream.close();
        } catch (Exception e) {
        }
        try {
            socket.close();
            appendLog("socket closed");
        } catch (Exception e) {
            appendLog("Error closing client socket.");
        }
        appendLog("Client is stopped.");
    }

    void informClientStopped() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                frame.informClientStopped();
            }
        });
    }

    void setComment(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                frame.setComment(text);
            }
        });
    }

    private int nLines;
    private int dRow;
    private int dCol;

    void setColors(int posR, int posC,
                   TreeMap <ClientGameDriver.Coord, Boolean> hWalls,
                   TreeMap <ClientGameDriver.Coord, Boolean> vWalls) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Line[][] vLines = frame.getVLines();
                Line[][] hLines = frame.getHLines();
                Color[] colors = frame.getColors();

                if (posR < dRow) dRow--;
                if (posC < dCol) dCol--;
                if (posR >= dRow + nLines) dRow++;
                if (posC >= dCol + nLines) dCol++;

                for (int r = 0; r < vLines.length; r++) {
                    for (int c = 0; c < vLines[r].length; c++) {
                        Boolean wall = null;
                        if (vWalls != null) {
                            wall = vWalls.get(gameDriver.new Coord(r + dRow, c + dCol));
                        }
                        int col = (wall == null) ? 0 : (wall ? 2 : 1);
                        vLines[r][c].setStroke(colors[col]);
                    }
                }
                for (int r = 0; r < hLines.length; r++) {
                    for (int c = 0; c < hLines[r].length; c++) {
                        Boolean wall = null;
                        if (hWalls != null) {
                            wall = hWalls.get(gameDriver.new Coord(r + dRow, c + dCol));
                        }
                        int col = (wall == null) ? 0 : (wall ? 2 : 1);
                        hLines[r][c].setStroke(colors[col]);
                    }
                }
                frame.setHim(posR - dRow, posC - dCol);
            }
        });
    }

    void setHisColor(int color) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                frame.setHisColor(color);
            }
        });
    }
}

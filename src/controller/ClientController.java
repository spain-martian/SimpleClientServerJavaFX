package controller;

import model.ClientGameDriver;
import javafx.application.Platform;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.TreeMap;

import model.PointRC;
import model.Message;
import view.ClientFrame;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 * Client's part controller
 */

public class ClientController {
    private ClientFrame frame;  //gui
    private ClientGameDriver gameDriver;    //game driver

    private Socket socket;
    private static final String hostName = "localhost";
    private ObjectOutputStream requestStream;
    private ObjectInputStream answerStream;
    private Thread clientThread;

    private int nScreenRows;        //maze size
    private int nScreenCols;
    private int addRow;
    private int addCol; //to get the real coordinates

    private boolean ballOut; //When the ball is out, the game is over

    public ClientController(ClientFrame frame, int nScreenRows, int nScreenCols) {
        this.frame = frame;
        this.nScreenRows = nScreenRows;
        this.nScreenCols = nScreenCols;
    }

    public boolean startClient() {

        if (!initializeGame()) {
            stopClient();
            return false;
        }

        addRow = 0;
        addCol = 0;
        gameDriver = new ClientGameDriver();

        frame.redrawMaze();

        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean end = false;
                    while (!end) {
                        frame.appendLog("Waiting for input.");
                        Message answer = (Message) answerStream.readObject();
                        frame.appendLog("message received: " + answer);
                        if (answer.getType() == Message.Type.END) {
                            end = true;
                        }
                        if (answer.getType() == Message.Type.ANSWER) {
                           react(answer);
                        }
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            //System.out.println("Client controller thread interrupted");
                            break;
                        }
                    }
                } catch (Exception ex) {
                    frame.appendLog("Error reading socket.");
                    //ex.printStackTrace();
                }
                stopClient();
            }
        });
        clientThread.start();

        return true;
    }

    private boolean initializeGame() {
        try {
            InetAddress ina = InetAddress.getByName(hostName);
            try {
                socket = new Socket(ina, frame.getPort());
                try {
                    requestStream = new ObjectOutputStream(socket.getOutputStream());
                    requestStream.flush();
                    answerStream = new ObjectInputStream(socket.getInputStream());

                    sendMessage(new Message(Message.Type.START, frame.getName()));
                    frame.appendLog("client: start message sent, waiting for answer.");

                    Message answer = (Message) answerStream.readObject();
                    if (answer.getType() == Message.Type.ANSWER) {
                        frame.appendLog("answer: " + answer);
                    } else {
                        if (answer.getType() == Message.Type.END) {
                            frame.appendLog("Server disconnected.");
                            return false;
                        } else {
                            frame.appendLog("Client: unexpected message: " + answer);
                        }
                    }
                } catch (Exception e) {
                    frame.appendLog("IO error " + e);
                    return false;
                }
            } catch (IOException ex) {
                frame.appendLog("Cannot connect to the host");
                return false;
            }
        } catch (UnknownHostException u) {
            frame.appendLog("Cannot find host name");
            return false;
        }
        return true;
    }

    public void react(Message message) {
        String comment;
        if (message.getType() == Message.Type.ANSWER) {
            String command = message.getData();
            if (command.startsWith("move ")) {
                char dest = command.charAt(5); //u, d, l, r
                if (command.endsWith("exit")) {
                    comment = "Congratulations! You have found the exit!";
                    gameDriver.addWall(dest, false);
                    gameDriver.move(dest);
                    ballOut = true;
                    updateMaze();
                } else {
                    if (command.endsWith("stopped")) {
                        comment = "You are out already.";
                    } else {
                        if (command.endsWith("yes")) {
                            gameDriver.addWall(dest, false);
                            gameDriver.move(dest);
                            comment = command.substring(0, 7) + ": moved";
                            updateMaze();
                        } else {
                            if (command.endsWith("no")) {
                                gameDriver.addWall(dest, true);
                                //System.out.println("Game no move: " + dest);
                                comment = command.substring(0, 7) + ": wall";
                                updateMaze();
                            } else {
                                comment = "Unrecognized command: " + command;
                            }
                        }
                    }
                }
                setComment(comment);
            }
        }
    }

    private void updateMaze() {
        int screenRow = gameDriver.getCurrentRow() + addRow;
        int screenCol = gameDriver.getCurrentCol() + addCol;
        if (screenRow < 0) {
            addRow -= screenRow;
        }
        if (screenCol < 0) {
            addCol -= screenCol;
        }
        if (screenRow >= nScreenRows) {
            addRow -= screenRow - nScreenRows + 1;
        }
        if (screenCol >= nScreenCols) {
            addCol -= screenCol - nScreenCols + 1;
        }
        //System.out.println("add r, c " + addRow + " " +  addCol);
        frame.redrawMaze();
    }

    public void sendMessage(Message message) {
        try {
            requestStream.writeObject(message);
            requestStream.flush();
            requestStream.reset();
            frame.appendLog(message + " sent.");
        } catch (IOException e) {
            frame.appendLog("Error sending message " + e);
        }
    }

    public void stopClient() {
        try {
            requestStream.close();
            answerStream.close();
        } catch (Exception ignore) {
        }
        try {
            socket.close();
            frame.appendLog("socket closed");
        } catch (Exception e) {
            frame.appendLog("Error closing client socket.");
        }
        if (clientThread != null) {
            clientThread.interrupt();
        }
        frame.informClientStopped();
    }

    public void setComment(String text) {
        frame.setComment(text);
    }

    public Boolean getHWall(int r, int c) {
        return gameDriver.getHWall(r - addRow, c - addCol);
    }

    public Boolean getVWall(int r, int c) {
        return gameDriver.getVWall(r - addRow, c - addCol);
    }

    public Boolean isCellVisited(int r, int c) {
        return gameDriver.isCellVisited(r - addRow, c - addCol);
    }

    public int getCurrentRow() {
        //System.out.println("scr r, c " + (gameDriver.getCurrentRow() + addRow) + " " +  (gameDriver.getCurrentCol() + addCol));
        return gameDriver.getCurrentRow() + addRow;
    }

    public int getCurrentCol() {
        //System.out.println("scr r, c " + (gameDriver.getCurrentRow() + addRow) + " " +  (gameDriver.getCurrentCol() + addCol));
        return gameDriver.getCurrentCol() + addCol;
    }

    public boolean isBallOut() {
        return ballOut;
    }
}

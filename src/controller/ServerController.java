package controller;

import javafx.application.Platform;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import model.GameDriver;
import model.ClientHandlingThread;
import view.ServerFrame;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 */
public class ServerController {
    public static final int defaultPort = 4434;
    public static final int maxNumClients = 3;
    private ServerFrame mainFrame;
    //private GameDriver gameDriver;
    private ServerSocket serverSocket;
    private ClientHandlingThread[] clientThreads;

    ServerController(ServerFrame frame) {
        mainFrame = frame;
    }

    boolean startServer(String textPort, String maxClients) {
        int port = checkPort(textPort);
        int max = checkMaxClients(maxClients);
        //gameDriver = new GameDriver();

        clientThreads = new ClientHandlingThread[max];
        mainFrame.getClientsList().clear();

        serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            appendLog("Server started on port: " + port);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean noproblem = true;
                    while (noproblem) {
                        try {
                            Socket socket = serverSocket.accept();
                            int i;
                            for (i = 0; i < clientThreads.length; i ++) {
                                if (clientThreads[i] == null) break;
                            }
                            if (i >= 0 && i < clientThreads.length) {
                                clientThreads[i] = new ClientHandlingThread(socket,
                                        ServerController.this, new GameDriver());
                                new Thread(clientThreads[i]).start();
                                addClientFromList(clientThreads[i].getName());
                            } else {
                                socket.close();
                            }
                        } catch (IOException ex) {
                            //appendLog("Problem accepting client socket.");
                            noproblem = false;
                        }
                    }
                }
            }).start();
        } catch (IOException io) {
            appendLog("Can not create server socket.");
            return false;
        }
        return true;
    }

    public int checkPort(String textPort) {
        int port = 0;
        try {
            port = Integer.parseInt(textPort);
        } catch (Exception ignore) {}
        if (port < 1024 || port > 65535) port = defaultPort;
        return port;
    }

    public int checkMaxClients(String text) {
        int max = 0;
        try {
            max = Integer.parseInt(text);
        } catch (Exception e) {}
        if (max < 1) max = 1;
        if (max > 6) max = 6;
        return max;
    }

    void disconnectAllClients() {
        if (clientThreads != null) {
            for (int i = 0; i < clientThreads.length; i++) {
                if (clientThreads[i] != null) {
                    removeClientFromList(clientThreads[i].getName());
                    disconnectClient(i);
                }
            }
            try {
                serverSocket.close();
            } catch (Exception e) {
            }
            appendLog("Server closed.");
        }
    }

    int getIndexForClientName(String name) {
        for (int i = 0; i < clientThreads.length; i ++) {
            if (clientThreads[i] != null) {
                if (clientThreads[i].getName().equals(name)) {
                    return i;
                }
            }
        }
        return -1;
    }

    boolean disconnectClient(String name) {
        int i = getIndexForClientName(name);
        if (i >= 0) {
            disconnectClient(i);
            removeClientFromList(name);
            return true;
        }
        return false;
    }

    private boolean disconnectClient(int i) {
        if (clientThreads[i] != null) {
            mainFrame.appendLog("Sending END to client.");
            clientThreads[i].sendEndToClient();
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
            }
            try {
                mainFrame.appendLog("Closing client socket.");
                clientThreads[i].closeSocket();
            } catch (Exception e) {
            }
            clientThreads[i] = null;
            return true;
        }
        return false;
    }


    void informThatClientDisconnected(String name) {
        removeClientFromList(name);
    }

    //Javafx cannot update UI from not the main thread
    private void removeClientFromList(String name) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // Update UI here.
                mainFrame.getClientsList().remove(name);
            }
        });
    }

    //Javafx cannot update UI from not the main thread
    private void addClientFromList(String name) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mainFrame.getClientsList().add(name);
            }
        });
    }

    void appendLog(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mainFrame.appendLog(text);
            }
        });
    }

    synchronized void changeClientName(String name, String newName) {
        removeClientFromList(name);
        addClientFromList(newName);
    }

    ClientHandlingThread[] getClientThreads() {
        return clientThreads;
    }
}

package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Platform;
import view.ServerFrame;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 * Listens for new clients.
 *
 * Creates new clients threads after setting up the connection
 *
 */
public class ServerController {
    public static final int defaultPort = 4434;
    public static final int maxNumClients = 3;
    private ServerFrame ui;
    private ServerSocket serverSocket;
    private Thread controllerThread;
    private List<ClientHandlingThread> clientThreads;

    public ServerController(ServerFrame frame) {
        ui = frame;
    }

    /**
     * Starts server
     *
     * @param textPort      port
     * @param maxClients    max clients
     * @return              true if success
     */
    public boolean startServer(String textPort, String maxClients) {
        int port = checkPort(textPort);
        int max = checkMaxClients(maxClients);

        clientThreads = new LinkedList<>();
        ui.refreshClients(new LinkedList<String>(), new LinkedList<String>());

        serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            ui.appendLog("Server started on port: " + port);
        } catch (Exception io) {
            ui.appendLog("Can not create server socket.");
            return false;
        }

        controllerThread = new Thread(() -> {
            while (true) {
                if (clientThreads.size() < max) {
                    try {
                        Socket socket = serverSocket.accept();
                        ClientHandlingThread clientThread = new ClientHandlingThread(socket, this, ui);
                        clientThread.start();
                        clientThreads.add(clientThread);
                        refreshGuiClients();
                    } catch (IOException ex) {
                        ui.appendLog("Problem accepting client socket.");
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    //System.out.println("Server controller thread interrupted");
                    break;
                }
            }
        });

        controllerThread.setName("ServerController");
        controllerThread.start();
        return true;
    }

    /**
     * Checks ports number that was inputted by user
     * In case of error sets default port
     *
     * @param textPort  input text with port number
     * @return          int value
     */
    public int checkPort(String textPort) {
        int port = 0;
        try {
            port = Integer.parseInt(textPort);
        } catch (Exception ignore) {}
        if (port < 1024 || port > 65535) port = defaultPort;
        return port;
    }

    /**
     * Checks max number of clients that was inputted by user
     * In case of error sets default value
     *
     * @param text  input text with max clients number
     * @return          int value
     */
    public int checkMaxClients(String text) {
        int numClients = 0;
        try {
            numClients = Integer.parseInt(text);
        } catch (Exception ignore) {}
        if (numClients < 1) numClients = 1;
        if (numClients > maxNumClients) numClients = maxNumClients;
        return numClients;
    }

    /**
     * Disconnects client with given name
     * @param name
     * @return
     */
    public synchronized boolean disconnectClient(String name) {
        for (ClientHandlingThread clientThread: clientThreads) {
            if (clientThread.getClientName().equals(name)) {
                clientThread.disconnect();
                ui.appendLog("Client " + name + " disconnected");
                clientThreads.remove(clientThread);
                refreshGuiClients();
                return true;
            }
        }
        return false;
    }

    /**
     * Disconnects all clients
     */
    public synchronized void disconnectAllClients() {
        if (clientThreads != null) {
            while (clientThreads.size() > 0) {
                disconnectClient(clientThreads.get(0).getClientName());
            }
        }
    }

    /**
     * Refreshes clients list in gui
     */
    public synchronized void refreshGuiClients() {
        Platform.runLater(() -> {
            try {
                Thread.sleep(300);
            } catch (Exception ignore) {}
            List<String> clients = new LinkedList<>();
            List<String> statuses = new LinkedList<>();

            //remove interrupted threads from the list
            for (int i = clientThreads.size() - 1; i >= 0; i--) {
                if (!clientThreads.get(i).isAlive()) {
                    clientThreads.remove(i);
                }
            }
            for (ClientHandlingThread clientThread : clientThreads) {
                clients.add(clientThread.getClientName());
                statuses.add(clientThread.getClientStatus());
            }
            ui.refreshClients(clients, statuses);
        });
    }

    /**
     * Closes current thread
     */
    public void closeThread() {
        try {
            serverSocket.close();
        } catch (Exception ignore) {}
        if(controllerThread != null) {
            controllerThread.interrupt();
        }
    }
}

package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import view.ServerFrame;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
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

    public int checkPort(String textPort) {
        int port = 0;
        try {
            port = Integer.parseInt(textPort);
        } catch (Exception ignore) {}
        if (port < 1024 || port > 65535) port = defaultPort;
        return port;
    }

    public int checkMaxClients(String text) {
        int numClients = 0;
        try {
            numClients = Integer.parseInt(text);
        } catch (Exception ignore) {}
        if (numClients < 1) numClients = 1;
        if (numClients > maxNumClients) numClients = maxNumClients;
        return numClients;
    }

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

    public synchronized void disconnectAllClients() {
        if (clientThreads != null) {
            while (clientThreads.size() > 0) {
                disconnectClient(clientThreads.get(0).getClientName());
            }
        }
    }

    public synchronized void refreshGuiClients() {
        List<String> clients = new LinkedList<>();
        List<String> statuses = new LinkedList<>();

        for (ClientHandlingThread clientThread: clientThreads) {
            clients.add(clientThread.getClientName());
            statuses.add(clientThread.getClientStatus());
        }
        ui.refreshClients(clients, statuses);
    }

    public synchronized void changeClientName(String name, String newName) {
        refreshGuiClients();
    }

    public void closeThread() {
        try {
            serverSocket.close();
        } catch (Exception ignore) {}
        if(controllerThread != null) {
            controllerThread.interrupt();
        }
    }
}

package controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import model.GameDriver;
import model.Message;
import view.ServerFrame;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 */

public class ClientHandlingThread extends Thread {
    private static int lastId;
    private int id;

    private ServerController controller;
    private GameDriver driver;
    private ServerFrame ui;

    private Socket socket;
    private String name;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public ClientHandlingThread(Socket socket, ServerController controller, ServerFrame ui) {
        this.socket = socket;
        this.controller = controller;
        this.ui = ui;
        driver = new GameDriver(controller);

        id = ++lastId;
        name = "#" + id;

        //driver.addPlayer(id); ///??? delete player
    }

    public void run() {
        boolean process = true;

        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            try {
                Message message = (Message) inputStream.readObject();
                if (message.getType() == Message.Type.START) {
                    ui.appendLog("Client's START message received: " + message.getData());
                    if (message.getData().length() > 0) {  //client's name
                        String newName = name + " " + message.getData();
                        controller.changeClientName(name, newName);
                        name = newName;
                    }
                    sendMessage(message.justAnswer("START accepted"));
                    ui.appendLog("Server start answer sent.");
                } else {
                    if (message.getType() == Message.Type.END) {
                        ui.appendLog("Client connection cancelled on client's side.");
                    } else {
                        ui.appendLog("Unexpected client message received: " + message);
                    }
                    process = false;
                }
            } catch (Exception e) {
                ui.appendLog("Error reading start message. " +  e);
                process = false;
            }

            while (process) {
                try {
                    Message message = (Message) inputStream.readObject();
                    if (message.getType() == Message.Type.REQUEST) {
                        ui.appendLog("Client message received-" + message);
                        Message answer = driver.getAnswer(message);
                        sendMessage(answer);
                        ui.appendLog("Server answer sent-" + answer);
                    } else {
                        if (message.getType() == Message.Type.END) {
                            ui.appendLog("End message received.");
                            process = false;
                        } else {
                            if (message.getType() == Message.Type.INFORM) {
                                ui.appendLog("Client's message received: " + message);
                            } else {
                                ui.appendLog("Unexpected client message type received: " + message + " - ignored");
                            }
                        }
                    }
                } catch (Exception e) {
                    ui.appendLog("Error reading or writing message. " + e);
                    process = false;
                }
            }
        } catch (IOException io) {
            ui.appendLog("Cannot open streams.");
        }

        disconnect();
    }

    public synchronized void sendMessage(Message message) throws IOException {
        outputStream.writeObject(message);
        outputStream.flush();
    }

    public void disconnect() {
        ui.appendLog("Sending 'END' to client.");
        try {
            Message message = new Message(Message.Type.END, "");
            sendMessage(message);
        } catch (Exception e) {
            ui.appendLog("Failed to send END to client " + e);
        }
        closeSocket();
    }

    public void closeSocket() {
        try {
            socket.close();
            ui.appendLog("Client socket closed.");
        } catch (Exception e) {
            ui.appendLog("Error closing client socket.");
        }
    }

    public String getClientName() {
        return name;
    }

    public String getClientStatus() {
        return driver.getStatus();
    }
}


package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import game.GameDriver;
import message.Message;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 */

public class ClientHandlingThread implements Runnable {
    private static int lastId;

    private int id;
    private Socket socket;
    private ServerController controller;
    private GameDriver driver;
    private String name;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    ClientHandlingThread(Socket socket, ServerController controller, GameDriver driver) {
        this.socket = socket;
        this.controller = controller;
        this.driver = driver;
        id = ++lastId;
        name = "#" + id;

        driver.addPlayer(id);
    }

    void closeSocket() {
        try {
            socket.close();
        } catch (Exception e) {
            controller.appendLog("Error closing client socket.");
        }
        controller.appendLog("Client socket closed.");
    }

    public String getName() {
        return name;
    }

    public void run() {
        boolean process = true;

        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            try {
                Message message = (Message) inputStream.readObject();
                if (message.getType() == Message.Type.START) {
                    controller.appendLog("Client's START message received: " + message.getData());
                    if (message.getData().length() > 0) {  //client's name
                        String newName = name + " " + message.getData();
                        controller.changeClientName(name, newName);
                        name = newName;
                    }
                    sendMessage(message.justAnswer("START accepted"));
                    controller.appendLog("Server start answer sent.");
                } else {
                    if (message.getType() == Message.Type.END) {
                        controller.appendLog("Client connection cancelled on client's side.");
                    } else {
                        controller.appendLog("Unexpected client message received: " + message);
                    }
                    process = false;
                }
            } catch (Exception e) {
                controller.appendLog("Error reading start message. " +  e);
                process = false;
            }

            while (process) {
                try {
                    Message message = (Message) inputStream.readObject();
                    if (message.getType() == Message.Type.REQUEST) {
                        controller.appendLog("Client message received-" + message);
                        Message answer = driver.getAnswer(id, message);
                        sendMessage(answer);
                        controller.appendLog("Server answer sent-" + answer);
                    } else {
                        if (message.getType() == Message.Type.END) {
                            controller.appendLog("End message received.");
                            process = false;
                        } else {
                            if (message.getType() == Message.Type.INFORM) {
                                controller.appendLog("Client's message received: " + message);
                            } else {
                                controller.appendLog("Unexpected client message type received: " + message);
                            }
                        }
                    }
                } catch (Exception e) {
                    controller.appendLog("Error reading or writing message. " + e);
                    process = false;
                }
            }
        } catch (IOException io) {
            controller.appendLog("Cannot open streams.");
        }

        closeSocket();
        controller.appendLog("Client thread ends: " + name);
        controller.informThatClientDisconnected(name);
    }

    synchronized void sendMessage(Message message) throws IOException {
        outputStream.writeObject(message);
        outputStream.flush();
    }

    void sendEndToClient() {
        try {
            Message message = new Message(Message.Type.END, "");
            sendMessage(message);
        } catch (Exception e) {
            controller.appendLog("Failed to send END to client " + e);
        }
    }

}


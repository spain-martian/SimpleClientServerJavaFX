package model;

import java.io.Serializable;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 */


public class Message implements Serializable {

    private static int counter;

    private int id;
    private Type type;
    private String data;

    public Message(Type type, String data) {
        this.type = type;
        this.data = data;
        id = ++counter;
    }

    private Message(Type type, String data, int id) {
        this.type = type;
        this.data = data;
        this.id = id;
    }

    public Message justAnswer(String text) {
        return new Message(Type.ANSWER, text, getId());
    }

    public Type getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "type= " + type + ", id= " + id + " " + data;
    }

    public enum Type {START, REQUEST, ANSWER, INFORM, END};
    // start: the 1st client's message. Data = client's name.
    // End: Client or server. Session ends.
    // Inform: does not require any answer.
    // Request: an answer is necessary
    // Answer: has id = id of the request!
}

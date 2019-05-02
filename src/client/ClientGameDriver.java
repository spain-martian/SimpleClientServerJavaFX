package client;

import message.Message;

import java.util.TreeMap;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 */

public class ClientGameDriver {
    private ClientController controller;

    private TreeMap <Coord, Boolean> hWalls = new TreeMap<>();
    private TreeMap <Coord, Boolean> vWalls = new TreeMap<>();

    private int posRow = 0;
    private int posCol = 0;
    private String comment;

    public ClientGameDriver(ClientController controller) {
        this.controller = controller;
    }

    public void react(Message message) {
        if (message.getType() == Message.Type.ANSWER) {
            String command = message.getData();
            if (command.startsWith("move ")) {
                if (command.endsWith("exit")) {
                    comment = "Congatulations! You have found the exit.";
                    move(command.charAt(5));
                    controller.setHisColor(1);
                } else {
                    if (command.endsWith("stopped")) {
                        comment = "You are out already.";
                    } else {
                        if (command.endsWith("yes")) {
                            addWall(command.charAt(5), false);
                            move(command.charAt(5));
                            comment = command.substring(0, 7) + ": moved";
                        } else {
                            if (command.endsWith("no")) {
                                addWall(command.charAt(5), true);
                                comment = command.substring(0, 7) + ": wall";
                            } else {
                                comment = "Unrecognized command: " + command;
                            }
                        }
                    }
                }
                controller.setComment(comment);
                controller.setColors(posRow, posCol, hWalls, vWalls);
            }
        }
    }

    private void addWall(char d, boolean wall) {
        if (d == 'u') {
            hWalls.put(new Coord(posRow, posCol), wall);
        }
        if (d == 'd') {
            hWalls.put(new Coord(posRow + 1, posCol), wall);
        }
        if (d == 'l') {
            vWalls.put(new Coord(posRow, posCol), wall);
        }
        if (d == 'r') {
            vWalls.put(new Coord(posRow, posCol + 1), wall);
        }
    }

    private void move(char d) {
        if (d == 'u') posRow --;
        if (d == 'd') posRow ++;
        if (d == 'l') posCol --;
        if (d == 'r') posCol ++;
    }

    class Coord implements Comparable{
        int r;
        int c;

        public Coord(int r, int c) {
            this.r = r;
            this.c = c;
        }

        @Override
        public int compareTo(Object o) {
            Coord other = (Coord) o;
            if (r > other.r) return 1;
            if (r < other.r) return -1;
            if (c > other.c) return 1;
            if (c < other.c) return -1;
            return 0;
        }
    }
}

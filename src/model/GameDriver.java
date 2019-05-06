package model;

import controller.ServerController;

import java.util.Random;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 */

public class GameDriver {
    private Maze maze;
    private static final int MIN_COL = 3;
    private static final int MIN_ROW = 3;
    private static final int MAX_COL = 7;
    private static final int MAX_ROW = 7;
    private static Random random = new Random();
    private String status = "Connected, not started";
    private ServerController controller;

    //player's position
    private PointRC point;

    public GameDriver (ServerController controller) {
        this(controller, MAX_ROW / 2, MAX_COL / 2);
    }

    private GameDriver (ServerController controller, int nRows, int nCols) {
        this.controller = controller;
        if (nCols < MIN_COL || nRows < MIN_ROW || nCols > MAX_COL || nRows > MAX_ROW) {
            throw new IllegalArgumentException("Maze size out of limits.");
        }
        // random size
        nRows = randomInLimits(MIN_ROW, nRows);
        nCols = randomInLimits(MIN_COL, nCols);
        maze = new Maze(nRows, nCols);
        maze.setRandomExit();

        point = new PointRC(randomInLimits(0, nRows), randomInLimits(0, nCols));
    }

    private int randomInLimits(int min, int max) {
        int n = max;
        if (min < max / 2) min = max / 2;
        if (max > min) {
            n = random.nextInt(max - min) + min;
        }
        return n;
    }

    public Message getAnswer(Message message) {
        if (message.getType() == Message.Type.REQUEST) {
            status = "Game started";
            controller.refreshGuiClients();
            if (message.getData().startsWith("move ")) {
                String command = message.getData().substring(0, 7); //ex: move dn
                int dest = 3;
                if (command.charAt(5) == 'u') dest = 0;
                if (command.charAt(5) == 'r') dest = 1;
                if (command.charAt(5) == 'd') dest = 2;

                if (!outOfMaze(point)) {
                    boolean yes = !maze.isWall(point.r, point.c, dest);
                    if (yes) {
                        if (dest == 0) point.r--;
                        if (dest == 1) point.c++;
                        if (dest == 2) point.r++;
                        if (dest == 3) point.c--;

                        if (outOfMaze(point)) {
                            status = "Exit found";
                            controller.refreshGuiClients();
                            return new Message(Message.Type.ANSWER, command + "=exit");
                        } else {
                            return new Message(Message.Type.ANSWER, command + "=yes");
                        }
                    } else {
                        return new Message(Message.Type.ANSWER, command + "=no");
                    }
                } else {
                    return new Message(Message.Type.ANSWER, command + "=game_stopped");
                }
            }
        }
        return new Message(Message.Type.ANSWER, "Request not recognized: " + message.getData());
    }

    private boolean outOfMaze(PointRC point) {
        return (point.r < 0 || point.r >= maze.getNumRows() || point.c < 0 || point.c >= maze.getNumCols());
    }
    
    public String getStatus() {
        return status;
    }
}

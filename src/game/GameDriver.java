package game;

import message.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 */

public class GameDriver {
    private Maze maze;
    private static final int MINC = 3;
    private static final int MINR = 3;
    private static final int MAXC = 30;
    private static final int MAXR = 30;
    private static Random random = new Random();

    private ArrayList<Player> players = new ArrayList<>();

    public GameDriver (int nRows, int nCols) {
        if (nCols < MINC || nRows < MINR || nCols > MAXC || nRows > MAXR) {
            throw new IllegalArgumentException("arguments out of limits.");
        }
        // random size
        nRows = randomInLimits(MINR, nRows);
        nCols = randomInLimits(MINC, nCols);
        maze = new Maze(nRows, nCols);
        maze.generateMaze(random.nextInt(nRows), random.nextInt(nCols));
        maze.setRandomExit();
        //System.out.println(maze);
    }

    public GameDriver () {
        this(MAXR / 2, MAXC / 2);
    }

    private int randomInLimits(int min, int max) {
        int n = max;
        if (min < max / 2) min = max / 2;
        if (max > min) {
            n = random.nextInt(max - min) + min;
        }
        return n;
    }

    public Message getAnswer(int id, Message message) {
        if (message.getType() == Message.Type.REQUEST) {
            if (message.getData().startsWith("move ")) {
                String command = message.getData().substring(0, 7); //move dn
                int dest = 3;
                if (command.charAt(5) == 'u') dest = 0;
                if (command.charAt(5) == 'r') dest = 1;
                if (command.charAt(5) == 'd') dest = 2;
                Player player = findPlayer(id);
                if (player == null) {
                    return new Message(Message.Type.ANSWER, "Bad player id: " + id);
                } else {
                    //System.out.println("before: " + player.r0 + " " + player.c0);
                    if (!player.out) {
                        boolean yes = !maze.isWall(player.r0, player.c0, dest);
                        if (yes) {
                            if (dest == 0) player.r0--;
                            if (dest == 1) player.c0++;
                            if (dest == 2) player.r0++;
                            if (dest == 3) player.c0--;
                            //System.out.println("after: " + player.r0 + " " + player.c0);

                            if (player.r0 < 0 || player.r0 >= maze.getNrRows()
                                    || player.c0 < 0 || player.c0 >= maze.getNrCols()) {
                                player.out = true;
                                return new Message(Message.Type.ANSWER, command + "=exit");
                            } else {
                                return new Message(Message.Type.ANSWER, command + "=yes");
                            }
                        } else {
                            return new Message(Message.Type.ANSWER, command + "=no");
                        }
                    } else {
                        return new Message(Message.Type.ANSWER, command + "=stopped");
                    }
                }
            }
        }
        return new Message(Message.Type.ANSWER, "Request not recognized: " + message.getData());
    }

    public void addPlayer(int id) {
        Player player = new Player(id);
        player.r0 = random.nextInt(maze.getNrRows());
        player.c0 = random.nextInt(maze.getNrCols());
        players.add(player);
    }

    private class Player {
        boolean out;  //true when gone
        int id;
        int r0;
        int c0;

        Player(int id) {
            this.id = id;
        }
    }

    private Player findPlayer(int id) {
        for (Player player: players) {
            if (player.id == id) return player;
        }
        return null;
    }

}

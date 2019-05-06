package model;

import controller.ClientController;
import view.ClientFrame;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Vadim Shutenko on 20-Aug-18.
 *
 */

public class ClientGameDriver {

    private TreeMap <PointRC, Boolean> hWalls = new TreeMap<>();
    private TreeMap <PointRC, Boolean> vWalls = new TreeMap<>();
    private Set<PointRC> visited = new HashSet<>();

    private int posRow = 0;  //current position
    private int posCol = 0;

    public ClientGameDriver() {
        visited.add(new PointRC(posRow, posCol));
    }

    public void addWall(char d, boolean isWall) {
        if (d == 'u') {
            hWalls.put(new PointRC(posRow, posCol), isWall);
        }
        if (d == 'd') {
            hWalls.put(new PointRC(posRow + 1, posCol), isWall);
        }
        if (d == 'l') {
            vWalls.put(new PointRC(posRow, posCol), isWall);
        }
        if (d == 'r') {
            vWalls.put(new PointRC(posRow, posCol + 1), isWall);
        }
    }

    public void move(char d) {
        if (d == 'u') posRow --;
        if (d == 'd') posRow ++;
        if (d == 'l') posCol --;
        if (d == 'r') posCol ++;
        //System.out.println("Game move: " + d + " (r, c)= " + posRow + " " + posCol);
        visited.add(new PointRC(posRow, posCol));
    }

    public Boolean getHWall(int r, int c) {
        PointRC point = new PointRC(r, c);
        return hWalls.get(point);
    }

    public Boolean getVWall(int r, int c) {
        PointRC point = new PointRC(r, c);
        return vWalls.get(point);
    }

    public boolean isCellVisited(int r, int c) {
        //System.out.println("vis rc" + r + " " + c+ " " + visited.contains(new PointRC(r, c)));
        return visited.contains(new PointRC(r, c));
    }

    public int getCurrentRow() {
        return posRow;
    }

    public int getCurrentCol() {
        return posCol;
    }
}

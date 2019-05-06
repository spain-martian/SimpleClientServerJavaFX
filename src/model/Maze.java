package model;

import java.util.Random;
import java.util.Stack;

/**
 * Created by Vadim Shutenko on 29-Aug-18.
 *
 * Class Maze creates and keeps a random maze
 */
public class Maze {
    private int numRows;
    private int numCols;
    private int[][] vWalls;
    private int[][] hWalls;
    private Random random = new Random();

    public Maze(int numRows, int numCols) {
        if (numCols < 3 || numRows < 3 || numCols > 100 || numRows > 100) {
            throw new IllegalArgumentException("Maze dimensions are out of limits (3-100)");
        }
        this.numRows = numRows;
        this.numCols = numCols;

        vWalls = new int[numRows][numCols + 1];
        hWalls = new int[numRows + 1][numCols];

        for (int x = 0; x < numCols + 1; x++) {
            for (int y = 0; y < numRows; y++) {
                vWalls[y][x] = 1;
            }
        }
        for (int x = 0; x < numCols; x++) {
            for (int y = 0; y < numRows + 1; y++) {
                hWalls[y][x] = 1;
            }
        }

        generateMaze(random.nextInt(numRows), random.nextInt(numCols));
    }

    /**
     * Generates maze beginning from (r0, c0) cell
     * @param r0
     * @param c0
     */
    private void generateMaze(int r0, int c0) {
        for (int x = 0; x < numCols + 1; x++) {
            for (int y = 0; y < numRows; y++) {
                vWalls[y][x] = 1;
            }
        }
        for (int x = 0; x < numCols; x++) {
            for (int y = 0; y < numRows + 1; y++) {
                hWalls[y][x] = 1;
            }
        }

        boolean[][] visited = new boolean[numRows][numCols];
        Stack<Cell> stack = new Stack<>();
        stack.push(new Cell(r0, c0));
        Cell[] cellsN = new Cell[4];  //for unvisited neighbours

        while (!stack.empty()) {
            Cell cell = stack.pop();
            int kN = 0;
            if (cell.r > 0 && !visited[cell.r - 1][cell.c]) {
                cellsN[kN++] = new Cell(cell.r - 1, cell.c);
            }
            if (cell.c > 0 && !visited[cell.r][cell.c - 1]) {
                cellsN[kN++] = new Cell(cell.r, cell.c - 1);
            }
            if (cell.r < numRows - 1 && !visited[cell.r + 1][cell.c]) {
                cellsN[kN++] = new Cell(cell.r + 1, cell.c);
            }
            if (cell.c < numCols - 1 && !visited[cell.r][cell.c + 1]) {
                cellsN[kN++] = new Cell(cell.r, cell.c + 1);
            }

            if (kN > 0) {
                int n = random.nextInt(kN);
                Cell neighbour = cellsN[n];
                //remove wall(current, neighbour);
                if (neighbour.r == cell.r) {
                    if (neighbour.c < cell.c) vWalls[cell.r][cell.c] = 0; //left
                    else vWalls[cell.r][cell.c + 1] = 0;    //right
                }
                if (neighbour.c == cell.c) { //left
                    if (neighbour.r < cell.r) hWalls[cell.r][cell.c] = 0; //up
                    else hWalls[cell.r + 1][cell.c] = 0;    //down
                }
                visited[neighbour.r][neighbour.c] = true;
                stack.push(cell);
                stack.push(neighbour);
            }
        }
    }

    public String toString() {
        String s = "";
        char blank = ' ';
        char cross = '+'; //'·';
        for (int r = 0; r < numRows + 1; r++) {
            for (int c = 0; c < numCols; c++) {
                s += cross;
                if (hWalls[r][c] > 0) s += '-';
                else s += blank;
            }
            s += cross + "\n";
            if (r == numRows) break;
            for (int c = 0; c < numCols + 1; c++) {
                if (vWalls[r][c] > 0) s += '|';
                else s += blank;
                s += blank;
            }
            s += "\n";
        }

        return s;
    }

    public int setRandomExit() {
        int limit = (numCols + numRows) * 2;
        int n = new Random().nextInt(limit);

        if (n < numCols) {
            hWalls[0][n] = 0;
        } else {
            if (n < 2 * numCols) {
                hWalls[numRows][n - numCols] = 0;
            } else {
                if (n < 2 * numCols + numRows) {
                    vWalls[n - 2 * numCols][0] = 0;
                } else {
                    vWalls[n - (2 * numCols + numRows)][numCols] = 0;
                }
            }
        }
        return n;
    }
    
    public boolean isWall(int r, int c, int dest) {  //0-up, 1-right, 2-down, 3-left
        if (r < 0 || c < 0 || r >= numRows || c >= numCols) {
            throw new IllegalArgumentException("arguments out of maze bounds " + numRows + ", " + numCols);
        }
        if (dest == 0 && hWalls[r][c] > 0
            || dest == 2 && hWalls[r + 1][c] > 0
            || dest == 1 && vWalls[r][c + 1] > 0
            || dest == 3 && vWalls[r][c] > 0)  {
            return true;
        }
        return false;
    }
    
    private class Cell {
        int r;
        int c;
        Cell(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    /*
    public static void main(String[] a) {
        Maze maze = new Maze(3,5);
        maze.generateMaze(2, 3);
        maze.setRandomExit();
        System.out.println(maze);
    }
    */

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

}

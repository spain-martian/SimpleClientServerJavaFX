package model;

import java.util.Random;
import java.util.Stack;

/**
 * Created by Vadim Shutenko on 29-Aug-18.
 */
public class Maze {
    private int nrRows;
    private int nrCols;

    private int[][] vWalls;
    private int[][] hWalls;

    public Maze(int nrRows, int nrCols) {
        if (nrCols < 2 || nrRows < 2 || nrCols > 100 || nrRows > 100) {
            throw new IllegalArgumentException("arguments out of limits (3-100)");
        }

        this.nrRows = nrRows;
        this.nrCols = nrCols;

        vWalls = new int[nrRows][nrCols + 1];
        hWalls = new int[nrRows + 1][nrCols];

        for (int x = 0; x < nrCols + 1; x++) {
            for (int y = 0; y < nrRows; y++) {
                vWalls[y][x] = 1;
            }
        }
        for (int x = 0; x < nrCols; x++) {
            for (int y = 0; y < nrRows + 1; y++) {
                hWalls[y][x] = 1;
            }
        }

    }

    public void generateMaze(int r0, int c0) {
        if (r0 < 0 || c0 < 0 || r0 >= nrRows || c0 >= nrCols) {
            throw new IllegalArgumentException("arguments out of maze limits " + nrRows + ", " + nrCols);
        }

        for (int x = 0; x < nrCols + 1; x++) {
            for (int y = 0; y < nrRows; y++) {
                vWalls[y][x] = 1;
            }
        }
        for (int x = 0; x < nrCols; x++) {
            for (int y = 0; y < nrRows + 1; y++) {
                hWalls[y][x] = 1;
            }
        }

        boolean[][] visited = new boolean[nrRows][nrCols];
        Random random = new Random();
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
            if (cell.r < nrRows - 1 && !visited[cell.r + 1][cell.c]) {
                cellsN[kN++] = new Cell(cell.r + 1, cell.c);
            }
            if (cell.c < nrCols - 1 && !visited[cell.r][cell.c + 1]) {
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
        for (int r = 0; r < nrRows + 1; r++) {
            for (int c = 0; c < nrCols; c++) {
                s += cross;
                if (hWalls[r][c] > 0) s += '-';
                else s += blank;
            }
            s += cross + "\n";
            if (r == nrRows) break;
            for (int c = 0; c < nrCols + 1; c++) {
                if (vWalls[r][c] > 0) s += '|';
                else s += blank;
                s += blank;
            }
            s += "\n";
        }

        return s;
    }

    public int setRandomExit() {
        int limit = (nrCols + nrRows) * 2;
        int n = new Random().nextInt(limit);

        if (n < nrCols) {
            hWalls[0][n] = 0;
        } else {
            if (n < 2 * nrCols) {
                hWalls[nrRows][n - nrCols] = 0;
            } else {
                if (n < 2 * nrCols + nrRows) {
                    vWalls[n - 2 * nrCols][0] = 0;
                } else {
                    vWalls[n - (2 * nrCols + nrRows)][nrCols] = 0;
                }
            }
        }
        return n;
    }
    
    public boolean isWall(int r, int c, int dest) {  //0-up, 1-right, 2-down, 3-left
        if (r < 0 || c < 0 || r >= nrRows || c >= nrCols) {
            throw new IllegalArgumentException("arguments out of maze limits " + nrRows + ", " + nrCols);
        }
        if (dest == 0 && hWalls[r][c] > 0) return true;
        if (dest == 2 && hWalls[r + 1][c] > 0) return true;
        if (dest == 1 && vWalls[r][c + 1] > 0) return true;
        if (dest == 3 && vWalls[r][c] > 0) return true;
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
    public int getNrRows() {
        return nrRows;
    }

    public int getNrCols() {
        return nrCols;
    }

}

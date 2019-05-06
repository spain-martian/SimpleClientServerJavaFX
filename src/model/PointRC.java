package model;

/**
 * Created by Vadim Shutenko on 03-May-19.
 */
public  class PointRC implements Comparable{
    int r;
    int c;

    public PointRC(int r, int c) {
        this.r = r;
        this.c = c;
    }

    @Override
    public int compareTo(Object o) {
        PointRC other = (PointRC) o;
        if (r > other.r) return 1;
        if (r < other.r) return -1;
        if (c > other.c) return 1;
        if (c < other.c) return -1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return r == ((PointRC) o).r && c == ((PointRC) o).c;
    }

    @Override
    public int hashCode() {
        return 1063 * r + c;
    }
}
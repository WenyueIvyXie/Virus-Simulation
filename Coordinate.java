/** Immutable row/column key for a neighbourhood in the sparse grid map. */
public class Coordinate {
    public final int row;
    public final int col;

    public Coordinate(int r, int c){
        this.row = r;
        this.col = c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;
        Coordinate c = (Coordinate) o;
        return row == c.row && col == c.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }
}

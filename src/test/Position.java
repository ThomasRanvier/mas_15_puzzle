package test;

public class Position {
    public int x;
    public int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "x:" + this.x + ",y:" + this.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Position pos = (Position) obj;
        return pos.x == this.x && pos.y == this.y;
    }

    @Override
    public int hashCode() {
        return this.x + (this.y * 10000);
    }
}

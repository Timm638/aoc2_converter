package de.timm638.aoc2_converter;

public class Point implements Comparable<Point> {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Returns points which lies in direction d
    public Point toDirection(Direction d) {
        return new Point(x + d.getXOffset(), y + d.getYOffset());
    }

    public Boolean isOutside(int maxX, int maxY) {
        return (x < 0 || x >= maxX || y < 0 || y >= maxY);
    }

    @Override
    public int compareTo(Point o) {
        final int yComp = Integer.compare(y, o.y);
        if (yComp != 0) {
            return yComp;
        }
        return Integer.compare(x, o.x);
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)", x, y);
    }
}

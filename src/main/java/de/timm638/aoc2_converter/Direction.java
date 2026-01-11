package de.timm638.aoc2_converter;

import static java.lang.Math.pow;

public enum Direction {
    EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORT_WEST, NORTH, NORTH_EAST;

    final protected int[] xOffset = {1, 1, 0, -1, -1, -1, 0, 1};
    final protected int[] yOffset = {0, 1, 1, 1, 0, -1, -1, -1};

    // Returns a power of 2
    public int getValue() {
        return 1 << this.ordinal();
    }

    public int getXOffset() {
        return xOffset[this.ordinal()];
    }

    public int getYOffset() {
        return yOffset[this.ordinal()];
    }

    // Returns direction 90 deg clockwise
    public Direction getCW() {
        return Direction.values()[(this.ordinal() + 2 + 8) % 8];
    }

    // Returns direction 90 deg counterclockwise
    public Direction getCCW() {
        return Direction.values()[(this.ordinal() - 2 + 8) % 8];
    }

    public static void debugPrintDirections () {
        String stencil = "%d: \t %s \t CW: %s \t CCW: %s \t x: %d \t y: %d \t value: %d";
        for (Direction d : Direction.values()) {
            System.out.printf((stencil) + "%n", d.ordinal(), d.toString(), d.getCW().toString(), d.getCCW().toString(), d.getXOffset(), d.getYOffset(), d.getValue());
        }
    }
}


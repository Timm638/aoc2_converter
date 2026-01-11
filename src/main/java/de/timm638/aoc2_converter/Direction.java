package de.timm638.aoc2_converter;

import static java.lang.Math.pow;

public enum Direction {
    EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST, NORTH, NORTH_EAST;

    final protected int[] xOffset = {1, 1, 0, -1, -1, -1, 0, 1};
    final protected int[] yOffset = {0, 1, 1, 1, 0, -1, -1, -1};

    public boolean isCardinal() {
        return this.ordinal() % 2 == 0;
    }

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
        return rotateCW(2);
    }

    // Returns direction 90 deg counterclockwise
    public Direction getCCW() {
        return rotateCW(-2);
    }

    // Returns next cardinal edge. For diagonal directions, it jump to the second next cardinal direction
    public Direction getCardinalCCW() {
        if (isCardinal()) {
            return getCCW();
        } else {
            return rotateCW(-3);
        }
    }

    // Increments of 45 deg
    public Direction rotateCW(int incrementsTimes45deg) {
        return Direction.values()[((this.ordinal() + incrementsTimes45deg) % 8 + 8) % 8];
    }
    // Increments of 45 deg
    public Direction rotateCCW(int incrementsTimes45deg) {
        return rotateCW(-incrementsTimes45deg);
    }

    public Direction reverse() {
        return rotateCW(4);
    }

    public static void debugPrintDirections () {
        String stencil = "%d: \t %s \t CW: %s \t CCW: %s \t x: %d \t y: %d \t value: %d";
        for (Direction d : Direction.values()) {
            System.out.printf((stencil) + "%n", d.ordinal(), d.toString(), d.getCW().toString(), d.getCCW().toString(), d.getXOffset(), d.getYOffset(), d.getValue());
        }
    }
}


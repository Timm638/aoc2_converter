package de.timm638.aoc2_converter;

import java.awt.image.BufferedImage;

public class Province {
	static int counter = 0;
	public int id = 0;
	
	public int[] x;
	public int[] y;

	private final Main main;

	public Province (Pixel[][] img, Main main, int originX, int originY) {
		id = counter;
		counter++;
		this.main = main;
		Pixel currentColor = img[originX][originY];

		// Init bit map
		// Shows which edges are outside of an blob
		// 0 = not an contour
		// 1 = top edge is outside
		// 4 = right edge is outside
		// 16 = bottom edge is outside
		// 64 = left edge is outside
		final int width = img.length;
		final int height = img[0].length;
		short[][] bitMap = new short[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				// Ignore pixels unequal to color
				if (img[x][y].compareTo(currentColor) != 0) {
					bitMap[x][y] = -1;
					continue;
				}
				// Calculate edges
				short b = 0;
				b += (short) (isSameColoured(img, currentColor, x, y - 1) ? 1 : 0);
				b += (short) (isSameColoured(img, currentColor, x + 1, y - 1) ? 2 : 0);
				b += (short) (isSameColoured(img, currentColor, x + 1, y) ? 4 : 0);
				b += (short) (isSameColoured(img, currentColor, x + 1 , y + 1) ? 8 : 0);
				b += (short) (isSameColoured(img, currentColor, x, y + 1) ? 16 : 0);
				b += (short) (isSameColoured(img, currentColor, x - 1, y + 1) ? 32 : 0);
				b += (short) (isSameColoured(img, currentColor, x - 1, y) ? 64 : 0);
				b += (short) (isSameColoured(img, currentColor, x - 1, y - 1) ? 128 : 0);
				bitMap[x][y] = b;
			}
		}

		// Generate a border from the bytemap
	}

	private boolean isSameColoured (Pixel[][] img, Pixel curColor, int x, int y) {
		// If outside, then they have to be included inthe border
		if (x < 0 || x >= img.length || y < 0 || y >= img[0].length) {
			return false;
		}
		// Explicit for easier debugging
		Pixel checkedColor = img[x][y];
		return curColor.compareTo(checkedColor) == 0;
	}
}

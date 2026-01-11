package de.timm638.aoc2_converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Province {
	static int counter = 0;
	public int id = 0;
	
	public int[] x;
	public int[] y;

	private final Main main;
	private final Pixel originPixel;
	private final Point origin;

	private final short[][] borderMap;
	private final List<Point> nodeList;

	// This constructor generates the bitM
	public Province (Pixel[][] img, Main main, Point origin) {
		id = counter;
		counter++;
		this.main = main;
		this.originPixel = img[origin.x][origin.y];
		this.origin = origin;
		// Init border map
		// Shows, which edge borders the outside of the province
		// 0 = all edges are inside the province
		// 1 = the top edge of the pixel is outside
		// 4 = right edge is outside
		// 16 = bottom edge is outside
		// 64 = left edge is outside
		borderMap = initBitmap(img);

		// Generate a border from the borderMap
		// We want to identify the outer border and list them in clockwise direction
		// After that, if any inner borders are left, then for each line of inner edge, we repeat the origin point and
		// paste them in counterclockwise direction. This shows up correct in the map editor
		nodeList = initEdges();
	}

	private short[][] initBitmap(Pixel[][] img) {
		final int width = img.length;
		final int height = img[0].length;
		short[][] arr = new short[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				// Ignore pixels unequal to province color
				if (img[x][y].compareTo(originPixel) != 0) {
					arr[x][y] = -1;
					continue;
				}
				// Calculate contours
				short b = 0;
				// Direction contains all 8 possible directions, encoded in a bit
				for (Direction d : Direction.values()) {
					b += (short) (isSameColoured(
							img,
							originPixel,
							x + d.getXOffset(),
							y + d.getYOffset()
					) ? 0 : d.getValue());
				}
				arr[x][y] = b;
			}
		}

		return arr;
	}

	private List<Point> initEdges () {
		ArrayList<Point> nodeList = new ArrayList<>();
		// Collect outer border
		nodeList.addAll(collectBorder(origin));
		// Collect inner border
		Point innerOriginPoint = findNextInnerStartPoint();
		while (innerOriginPoint != null) {
			// Add origin point as anchor point to reset the position between each inner edge
			nodeList.add(origin);
			// Collect points and make them counter clock wise
			List<Point> innerEdges = collectBorder(innerOriginPoint);
			Collections.reverse(innerEdges);
			nodeList.addAll(innerEdges);
			// Prepare for next iteration
			innerOriginPoint = findNextInnerStartPoint();
		}
		return nodeList;
	}

	// Generate an edge map and consumes the border map. Returns in clockwise order
	private List<Point> collectBorder(Point startPoint) {
		ArrayList<Point> nodeList = new ArrayList<>();
		// Add start
		nodeList.add(startPoint);
		// Walk along the border
		Direction curDirection = Direction.EAST;
		Direction previousDirection = null;

		// We check if we have a border here
		// 8 cases exists, we fetch the pixel into the current direction and check in the ccw of the current if there exists an edge
		// 	 - If yes, then we continue in that direction
		//   - If no, we continue to turn

		// This don't directly write down the nodes, but keeps track the two previous points and first write a point if the direction changes
		// This prevents of multiple neighbouring points being in a line

		return nodeList;
	}

	// Returns null if no one is found
	private Point findNextInnerStartPoint() {
		final int width = borderMap.length;
		final int height = borderMap[0].length;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				short value = borderMap[x][y];
				if (value > 0) {
					return new Point(x, y);
				}
			}
		}
		// No origin point found
		return null;
	}

	private static boolean isSameColoured (Pixel[][] img, Pixel curColor, int x, int y) {
		// If outside, then they have to be included inthe border
		if (x < 0 || x >= img.length || y < 0 || y >= img[0].length) {
			return false;
		}
		// Explicit for easier debugging
		Pixel checkedColor = img[x][y];
		return curColor.compareTo(checkedColor) == 0;
	}
}

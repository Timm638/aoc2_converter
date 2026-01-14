package de.timm638.aoc2_converter;

import org.jfree.svg.SVGGraphics2D;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Province {
	static int counter = 0;
	public int id = 0;

	private final Main main;
	private final Pixel originPixel;
	private final Point origin;

	private final short[][] borderMap;
	private final List<Point> nodeList;

	private final Direction[] cardinalDirections = new Direction[]{Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};

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
		exportBorderMapToSVG(String.format("%d_boerderMap.svg", id), null, null, null);

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
					if (!d.isCardinal()) {
						continue;
					}
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
	private List<Point> collectBorder(final Point startPoint) {
		ArrayList<Point> nodeList = new ArrayList<>();
		// We initialize the start state
		Direction initialEdgeDirection = findFreeEdge(startPoint);
		Direction startDirection = initialEdgeDirection.rotateCCW(1);
		Direction curDirection = startDirection;
		Point curPoint = startPoint;
		//nodeList.add(removeEdgeFromMap(startPoint, initialEdgeDirection));
		Direction previousDirection;
		Point previousPoint;


		// We check if we have a border here
		// 8 cases exists, we fetch the pixel into the current direction and check in the ccw of the current if there exists an edge
		// 	 - If yes, then we continue in that direction
		//   - If no, we continue to turn
		// In this loop we process a block
		while (true) {
			// Find the next edge
			Direction curEdgeDirection;
			Point curEdgePoint;
			boolean edgeExists;
			boolean reachedStartState;
			do {
				curDirection = curDirection.rotateCW(1);
				curEdgeDirection = curDirection.getCardinalCCW();
				curEdgePoint = curPoint.toDirection(curDirection);
				// We have to not only check, if the edge exits, but also if that edge is part of our same group.
				edgeExists = isEdgeOnBlock(curEdgePoint, curEdgeDirection);
				// If diagonal, we have to check if the cardinal block between it is filled, otherwise we skip that edge.
				if (!curDirection.isCardinal()) {
					Point connectingPoint = curPoint.toDirection(curEdgeDirection.reverse());
					edgeExists = edgeExists && borderMap[connectingPoint.x][connectingPoint.y] != -1;
				}

				if (!edgeExists && curDirection.isCardinal()) {
					// The next turns around the corner, we have to add an point
					nodeList.add(removeEdgeFromMap(curPoint, curDirection));
				} else if (edgeExists && !curDirection.isCardinal()) {
					nodeList.add(removeEdgeFromMap(curEdgePoint, curEdgeDirection));
				}
				reachedStartState = curEdgePoint.compareTo(startPoint) == 0 && countEdgesOnBlock(startPoint) == 0;
			} while (!edgeExists && !reachedStartState);

			// TODO: Check if we even have to put prev into the node list
			// Set up initial state for next edge search
			previousPoint = curPoint;
			previousDirection = curDirection;
			curPoint = curPoint.toDirection(curDirection);
			curDirection = curDirection.rotateCCW(3);

			exportBorderMapToSVG(String.format("%d_boerderMap.svg", id), nodeList, previousPoint, curPoint);

			// End the loop, if we are again at the start
			if (reachedStartState) {
				break;
			}
		}
		// This don't directly write down the nodes, but keeps track the two previous points and first write a point if the direction changes
		// This prevents of multiple neighbouring points being in a line

		return nodeList;
	}

	private int countEdgesOnBlock(Point p) {
		int i = 0;
		for (Direction direction : cardinalDirections) {
			if (isEdgeOnBlock(p, direction)) {
				i += 1;
			}
		}
		return i;
	}

	// edgeDir is along the normal of the edge
	private Point getFirstCorner(Point curPoint, Direction edgeDir) {
		switch (edgeDir) {
			case NORTH:
				return curPoint;
			case EAST:
				return new Point(curPoint.x + 1, curPoint.y);
			case SOUTH:
				return new Point(curPoint.x + 1, curPoint.y + 1);
			case WEST:
				return new Point(curPoint.x, curPoint.y + 1);
			case NORTH_EAST:
			case NORTH_WEST:
			case SOUTH_EAST:
				case SOUTH_WEST:
					throw new NotImplementedException();

		}
		return null;
	}

	private Direction findFreeEdge(Point point) {
		for (Direction d : cardinalDirections) {
			if (isEdgeOnBlock(point, d)) {
				return d;
			}
		}
		return null;
	}

	// Removes edge from borderMap as soon it is processed
	// Returns point to put into the nodelist
	private Point removeEdgeFromMap(Point curPoint, Direction edgeDirection) {
		// The value for the current block and both of it's neighbours have to be updates
		Point anchorPoint = curPoint.toDirection(edgeDirection);
		removeEdgeFromMapSingleCell(anchorPoint, edgeDirection);
		return getFirstCorner(curPoint, edgeDirection);
	}

	private void removeEdgeFromMapSingleCell(Point anchorPoint, Direction edgeDirection) {
		Point curPoint = anchorPoint.toDirection(edgeDirection.reverse());
		if (curPoint.isOutside(borderMap.length, borderMap[0].length) || borderMap[curPoint.x][curPoint.y] == -1) {
			return;
		}
		borderMap[curPoint.x][curPoint.y] -= (short) edgeDirection.getValue();
	}

	private Boolean isEdgeOnBlock(Point checkPoint, Direction edgeDirection) {
		if (checkPoint.isOutside(borderMap.length, borderMap[0].length)) {
			return false;
		}
		short value = borderMap[checkPoint.x][checkPoint.y];
		if (value < 0) {
			return false;
		}
		value &= (short) (1 << edgeDirection.ordinal());
		return value != 0;
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
		if (new Point(x, y).isOutside(img.length, img[0].length)) {
			return false;
		}
		// Explicit for easier debugging
		Pixel checkedColor = img[x][y];
		return curColor.compareTo(checkedColor) == 0;
	}

	// Writes generated corners to the file named after province id
	public void exportToFile () throws IOException {
		FileWriter fw = null;
		fw = new FileWriter(String.valueOf(this.id));
		BufferedWriter bw = new BufferedWriter(fw);
		final int nSize = nodeList.size();
		for (int i = 0; i < nSize; ++i) {
			bw.write(String.valueOf(nodeList.get(i).y * main.scale) + (i != nSize - 1 ? "," : ""));
		}
		bw.write(";");
		for (int i = 0; i < nSize; ++i) {
			bw.write(String.valueOf(nodeList.get(i).y * main.scale) + (i != nSize - 1 ? "," : ""));
		}
		bw.close();
		fw.close();
	}

	private void exportBorderMapToSVG(String file, List<Point> nodes, Point prevPoint, Point curPoint) {
		final int scaling = 10;
		final int width = borderMap.length;
		final int height = borderMap[0].length;
		Color solid = new Color(originPixel.r, originPixel.g, originPixel.b, 255);
		Color semi = solid.darker();
		SVGGraphics2D g2 = new SVGGraphics2D(width * scaling, height * scaling);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				short v = borderMap[x][y];
				if (v == -1) {
					continue;
				}
				else if (v > 0) {
					g2.setPaint(solid);
				} else if (v == 0) {
					g2.setPaint(semi);
				}
				g2.fillRect(x * scaling, y * scaling, 10, 10);
				if (v > 0) {
					g2.setPaint(semi);
					final int xs = x * scaling;
					final int ys = y * scaling;
					final int hs = scaling / 2;
					final int fs = scaling;
					if (isEdgeOnBlock(new Point(x, y), Direction.EAST)) {
						g2.fillPolygon(new int[]{xs + fs, xs + fs, xs + hs}, new int[]{ys, ys + fs, ys + hs}, 3);
					}
					if (isEdgeOnBlock(new Point(x, y), Direction.SOUTH)) {
						g2.fillPolygon(new int[]{xs + fs, xs, xs + hs}, new int[]{ys + fs, ys + fs, ys + hs}, 3);
					}
					if (isEdgeOnBlock(new Point(x, y), Direction.WEST)) {
						g2.fillPolygon(new int[]{xs, xs, xs + hs}, new int[]{ys + fs, ys, ys + hs}, 3);
					}
					if (isEdgeOnBlock(new Point(x, y), Direction.NORTH)) {
						g2.fillPolygon(new int[]{xs, xs + fs, xs + hs}, new int[]{ys, ys, ys + hs}, 3);
					}
				}
			}
		}
		if (nodes != null) {
			g2.setColor(Color.yellow);
			for (Point p : nodes) {
				g2.fillOval(p.x * scaling - scaling/4, p.y * scaling - scaling/4, scaling/2, scaling/2);
			}
		}
		if (prevPoint != null) {
			g2.setColor(Color.blue);
			g2.fillOval(prevPoint.x * scaling - scaling/4 + scaling/2, prevPoint.y * scaling - scaling/4 + scaling/2, scaling/2, scaling/2);
		}
		if (curPoint != null) {
			g2.setColor(Color.green);
			g2.fillOval(curPoint.x * scaling - scaling/4 + scaling/2, curPoint.y * scaling - scaling/4 + scaling/2, scaling/2, scaling/2);
		}
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(g2.getSVGDocument());
			bw.close();
			fw.close();
		} catch (Exception e) {

		}
	}
}

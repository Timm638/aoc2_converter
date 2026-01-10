package de.timm638.aoc2_converter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

public class Main {

	@Parameter(names={"--scale", "-s"}, description = "Modifier to scale image width")
	public int scale = 1;

	@Parameter(converter = FileConverter.class, description = "Path to an image file", required = true)
	File inputImage;
	private int height;
	private int width;

	@Parameter(names={"--verbose", "-v"}, description = "Prints progress to console")
	Boolean verbose = Boolean.FALSE;

	int[][] provinceMap;

	private void print(String str) {
		if (verbose) {
			System.out.println(str);
		}
	}

	public static void main (String[] args) {
		Main main = new Main();
		JCommander.newBuilder()
				.addObject(main)
				.build()
				.parse(args);

		// For benchmarking
		long startTime = System.currentTimeMillis();
		main.start();
		long endTime = System.currentTimeMillis();
		System.out.printf("The programm ran for %d ms\n", endTime - startTime);
	}

	public void start () {
        BufferedImage img;
        try {
            img = ImageIO.read(inputImage);
			width = img.getWidth();
			height = img.getHeight();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



		Pixel[][] pixels = convertImageToArray(img);

		LinkedList<Province> provinceList = new LinkedList<Province>();

		int y = 0;
		int x = 0;
		while (y < height) {

			if (pixels[x][y] != null) {

				print("Generating Province #" + provinceList.size() + " of Color " + pixels[x][y].returnAsText());
				Province prov = new Province(pixels, this, x, y);

				LinkedList<Pixel> truePix = null;
				getBlobOfColor(pixels, x, y, prov.id);
				LinkedList<Pixel> reallyTruePix = getContourOfBlob(pixels, truePix);

				String s = "";
				for (Pixel pix : reallyTruePix) {
					s += "[" + pix.x + "," + pix.y + "],";
				}
				print(s);

				if (reallyTruePix.size() < 2) {
					print("WARNING: One pixel province detected, replace it");
				}

				//TODO: Better algorithm to convert to node


				Edges nodeDirection = returnEdgeByDirection(pixels, reallyTruePix);


				String s2 = "";
				for (Byte dir: nodeDirection.directions) {
					s2 += "[" + dir +"],";
				}
				print(s2);

				LinkedList<Integer> nodeX = new LinkedList<Integer>();
				LinkedList<Integer> nodeY = new LinkedList<Integer>();

				NodeList nl = generateNodeFromDirEdge(pixels, nodeDirection);
				nodeX = nl.nodeX;
				nodeY = nl.nodeY;

				String s3 = "";
				for (int dir: nodeX) {
					if (dir < 10) {
						s3 += "[ " + dir +"],";
					} else {
						s3 += "[" + dir +"],";
					}

				}
				print(s3);

				String s4 = "";
				for (int dir: nodeY) {
					if (dir < 10) {
						s4 += "[ " + dir +"],";
					} else {
						s4 += "[" + dir +"],";
					}
				}
				print(s4);

				NodeList cleanedUpNodes = cleanupList(new NodeList(nodeX, nodeY));
				nodeX = cleanedUpNodes.nodeX;
				nodeY = cleanedUpNodes.nodeY;

				s3 = "";
				for (int dir: nodeX) {
					if (dir < 10) {
						s3 += "[ " + dir +"],";
					} else {
						s3 += "[" + dir +"],";
					}

				}
				print(s3);

				s4 = "";
				for (int dir: nodeY) {
					if (dir < 10) {
						s4 += "[ " + dir +"],";
					} else {
						s4 += "[" + dir +"],";
					}
				}
				print(s4);

				int[] nodeXArr = new int[nodeX.size()];
				for (int nXi = 0; nXi < nodeX.size(); nXi++) {
					nodeXArr[nXi] = nodeX.get(nXi) * scale;
				}
				int[] nodeYArr = new int[nodeY.size()];
				for (int nYi = 0; nYi < nodeY.size(); nYi++) {
					nodeYArr[nYi] = nodeY.get(nYi) * scale;
				}
				// provinceList.add(new Province(nodeXArr,nodeYArr));

				for (Pixel pix : truePix) {
						pixels[pix.x][pix.y] = null;
				}


			} else {
				x++;
				if (x >= width) {
					x = 0;
					y++;
				}
			}
		}

		for (Province prov : provinceList) {
			FileWriter fw = null;
	        try {
	            int i;
	            fw = new FileWriter(prov.id + "");
	            BufferedWriter bw = new BufferedWriter(fw);
	            int nSize = prov.x.length;
	            for (i = 0; i < nSize; ++i) {
	                bw.write("" + prov.x[i] + (i != nSize - 1 ? "," : ""));
	            }
	            bw.write(";");
	            nSize = prov.y.length;
	            for (i = 0; i < nSize; ++i) {
	                bw.write("" + prov.y[i] + (i != nSize - 1 ? "," : ""));
	            }
	            bw.close();
	            fw.close();
	        }
	        catch (IOException ex) {
	            print("AoCC");
	        }
		}

			print("Finished!");
		/*
		for (int y = 0; y < height; y++) {
			String s = y + ": ";
			for (int x = 0; x < width; x++) {
				s += "[" + pixels[x][y].returnAsText() + "], ";
			}
			print(s);
		} */
	}

	private Pixel[][] convertImageToArray (BufferedImage image) {
		Raster raster = image.getRaster();
		final int width = image.getWidth();
		final int height = image.getHeight();
		Pixel[][] pixels = new Pixel[width][height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int[] pixelData = raster.getPixel(x, y, new int[4]);
				pixels[x][y] = new Pixel(pixelData, x, y);
			}
		}

		return pixels;
	}

	// In cardinal directions
	private Pixel[] getCardinalNeighbours(Pixel[][] pixels, int x, int y) {
		Pixel [] arr = {null, null, null, null};
		if (x > 0) {
			arr[0] = pixels[x - 1][y];
		}
		if (x < pixels.length - 1) {
			arr[1] = pixels[x + 1][y];
		}
		if (y > 0) {
			arr[2] = pixels[x][y - 1];
		}
		if (y < pixels[0].length - 1) {
			arr[3] = pixels[x][y + 1];
		}
		return arr;
	}

	private Integer getBlobOfColor (Pixel[][] pixels, int x, int y, int new_id) {
		final Pixel startPixel = pixels[x][y];
		LinkedList<Pixel> closedPixel = new LinkedList<>();
		LinkedList<Pixel> openPixel = new LinkedList<>();

		openPixel.add(pixels[x][y]);

		while (!openPixel.isEmpty()) {
			Pixel curPixel = openPixel.remove();
			closedPixel.add(curPixel);
			Pixel[] neighbours = getCardinalNeighbours(pixels, curPixel.x, curPixel.y);
			for (Pixel p : neighbours) {
				if (p == null)
					continue;

				if (!closedPixel.contains(p) && !openPixel.contains(p)) {
					if (p.compareTo(startPixel) == 0) {
						openPixel.add(p);
						provinceMap[p.x][p.y] = new_id;
					} else {
						closedPixel.add(p);
					}
				}
			}
		}
		return new_id;
	}

	private LinkedList<Pixel> getBlobSurroundedByColor (Pixel[][] pixels, String cmp, int x, int y) {

		int width = pixels.length;
		int height = pixels[0].length;

		LinkedList<Pixel> closedPixel = new LinkedList<>();
		LinkedList<Pixel> openPixel = new LinkedList<>();

		LinkedList<Pixel> blobPixels = new LinkedList<>();

		openPixel.add(pixels[x][y]);
		blobPixels.add(pixels[x][y]);

		while (!openPixel.isEmpty()) {
			Pixel curPixel = openPixel.remove();
			curPixel.opened = false;
			closedPixel.add(curPixel);
			curPixel.closed = true;
			if (!curPixel.returnAsText().equals(cmp)) {
				Pixel nextPix;
				if (curPixel.x > 0) {
					nextPix = pixels[curPixel.x - 1][curPixel.y];
					if (nextPix != null) {
						if (!(nextPix.closed || nextPix.opened)) {
						openPixel.add(nextPix);
						nextPix.opened = true;
						}
					}
				}
				if (curPixel.x < width - 1) {
					nextPix = pixels[curPixel.x + 1][curPixel.y];
					if (nextPix != null) {
						if (!(nextPix.closed || nextPix.opened)) {
						openPixel.add(nextPix);
						nextPix.opened = true;
						}
					}
				}
				if (curPixel.y > 0) {
					nextPix = pixels[curPixel.x][curPixel.y - 1];
					if (nextPix != null) {
						if (!(nextPix.closed || nextPix.opened)) {
						openPixel.add(nextPix);
						nextPix.opened = true;
						}
					}
				}
				if (curPixel.y < height - 1) {
					nextPix = pixels[curPixel.x][curPixel.y + 1];
					if (nextPix != null) {
						if (!(nextPix.closed || nextPix.opened)) {
						openPixel.add(nextPix);
						nextPix.opened = true;
						}
					}
				}
			}
		}

		LinkedList<Pixel> truePix = new LinkedList<Pixel>();
		for (Pixel pix : closedPixel) {
			if (!pix.returnAsText().equals(cmp)) {
				truePix.add(pix);
			}
		}

		for (Pixel pix : closedPixel) {
			pix.closed = false;
		}
		for (Pixel pix : openPixel) {
			pix.opened = false;
		}

		return truePix;
	}

	private LinkedList<Pixel> getContourOfBlob(Pixel[][] pixels, LinkedList<Pixel> truePix) {

		int width = pixels.length;
		int height = pixels[0].length;

		LinkedList<Pixel> reallyTruePix = new LinkedList<Pixel>();
		for (Pixel pix : truePix) {
			if (pix.x > 0) {
				if (pixels[pix.x - 1][pix.y] != null) {
					if (!pixels[pix.x - 1][pix.y].returnAsText().equals(pix.returnAsText())) {
						reallyTruePix.add(pix);
						continue;
					}
				} else {
					reallyTruePix.add(pix);
					continue;
				}
			}
			else {
				reallyTruePix.add(pix);
				continue;
			}
			if (pix.x < width - 1) {
				if (pixels[pix.x + 1][pix.y] != null) {
					if (!pixels[pix.x + 1][pix.y].returnAsText().equals(pix.returnAsText())) {
						reallyTruePix.add(pix);
						continue;
					}
				} else {
					reallyTruePix.add(pix);
					continue;
				}
			}
			else {
				reallyTruePix.add(pix);
				continue;
			}

			if (pix.y > 0) {
				if (pixels[pix.x][pix.y - 1] != null) {
					if (!pixels[pix.x][pix.y - 1].returnAsText().equals(pix.returnAsText())) {
						reallyTruePix.add(pix);
						continue;
					}
				} else {
					reallyTruePix.add(pix);
					continue;
				}
			}
			else {
				reallyTruePix.add(pix);
				continue;
			}
			if (pix.y < height - 1) {
				if (pixels[pix.x][pix.y  + 1] != null) {
					if (!pixels[pix.x][pix.y + 1].returnAsText().equals(pix.returnAsText())) {
						reallyTruePix.add(pix);
						continue;
					}
				} else {
					reallyTruePix.add(pix);
					continue;
				}
			}
			else {
				reallyTruePix.add(pix);
				continue;
			}
		}
		return reallyTruePix;
	}

private LinkedList<Pixel> getBorderOfMixedBlob (Pixel[][] pixels, LinkedList<Pixel> truePix, String cmpString) {

		int width = pixels.length;
		int height = pixels[0].length;

		LinkedList<Pixel> reallyTruePix = new LinkedList<Pixel>();
		for (Pixel pix : truePix) {
			if (pix.x > 0) {
				if (pixels[pix.x - 1][pix.y] != null) {
					if (pixels[pix.x - 1][pix.y].returnAsText().equals(cmpString)) {
						reallyTruePix.add(pix);
						continue;
					}
				} else {
					reallyTruePix.add(pix);
					continue;
				}
			}
			else {
				reallyTruePix.add(pix);
				continue;
			}
			if (pix.x < width - 1) {
				if (pixels[pix.x + 1][pix.y] != null) {
					if (pixels[pix.x + 1][pix.y].returnAsText().equals(cmpString)) {
						reallyTruePix.add(pix);
						continue;
					}
				} else {
					reallyTruePix.add(pix);
					continue;
				}
			}
			else {
				reallyTruePix.add(pix);
				continue;
			}

			if (pix.y > 0) {
				if (pixels[pix.x][pix.y - 1] != null) {
					if (pixels[pix.x][pix.y - 1].returnAsText().equals(cmpString)) {
						reallyTruePix.add(pix);
						continue;
					}
				} else {
					reallyTruePix.add(pix);
					continue;
				}
			}
			else {
				reallyTruePix.add(pix);
				continue;
			}
			if (pix.y < height - 1) {
				if (pixels[pix.x][pix.y  + 1] != null) {
					if (pixels[pix.x][pix.y + 1].returnAsText().equals(cmpString)) {
						reallyTruePix.add(pix);
						continue;
					}
				} else {
					reallyTruePix.add(pix);
					continue;
				}
			}
			else {
				reallyTruePix.add(pix);
				continue;
			}
		}
		return reallyTruePix;
	}

	private Edges returnEdgeByDirection(Pixel[][] pixels, LinkedList<Pixel> reallyTruePix) {

		Edges edges = null;

		int width = pixels.length;
		int height = pixels[0].length;

		LinkedList<Byte> nodeDirection = new LinkedList<Byte>();
		LinkedList<Pixel> pixHistory = new LinkedList<Pixel>();

		Pixel eP = null;
		outer:
		for (int startY = 0; startY < height; startY++) {
			for (int startX = 0; startX < width; startX++) {
				if (reallyTruePix.contains(pixels[startX][startY])) {
					eP = pixels[startX][startY];
					break outer;
				}
			}
		}

		Pixel startPixel = eP;
		int dirOffset = 0;
		int historyOffset = 0;

		outer:
		while(!reallyTruePix.isEmpty()) {
			String[] posToSearch = {
					(eP.x + 0) + "," + (eP.y - 1),
					(eP.x + 1) + "," + (eP.y - 1),
					(eP.x + 1) + "," + (eP.y + 0),
					(eP.x + 1) + "," + (eP.y + 1),
					(eP.x + 0) + "," + (eP.y + 1),
					(eP.x - 1) + "," + (eP.y + 1),
					(eP.x - 1) + "," + (eP.y + 0),
					(eP.x - 1) + "," + (eP.y - 1),
					};
			if (dirOffset < 0) {
				dirOffset = dirOffset % 8 + 8;
			}
			for (byte curDir = 0; curDir < posToSearch.length; curDir++) {
				for (Pixel px : reallyTruePix) {
					byte trueDir = ((byte)((curDir + dirOffset) % posToSearch.length));
					if (trueDir < 0) trueDir += 8;
					if (true ) { // px.returnPos().equals(posToSearch[trueDir])

						nodeDirection.add(trueDir);
						dirOffset = trueDir - 3;
						historyOffset = 0;
						pixHistory.add(eP);
						if (reallyTruePix.contains(eP)) {
						reallyTruePix.remove(eP);
						}
						eP = px;
						continue outer;
					} else if (true && pixHistory.size() != 0) { // startPixel.returnPos().equals(posToSearch[trueDir]
						edges = new Edges(startPixel.x, startPixel.y, nodeDirection);
						edges.innerEdges = new LinkedList<Edges>();

						if (reallyTruePix.contains(eP)) {
							reallyTruePix.remove(eP);
							}

						Pixel nextPix = null;

						while (!reallyTruePix.isEmpty()) {
							for (int startY = 0; startY < height; startY++) {
								for (int startX = 0; startX < width; startX++) {
									if (reallyTruePix.contains(pixels[startX][startY])) {
										nextPix = pixels[startX][startY];
										LinkedList<Pixel> containingPix = getBlobSurroundedByColor(pixels, startPixel.returnAsText(), nextPix.x, nextPix.y + 1);
										LinkedList<Pixel> borderPix = getBorderOfMixedBlob(pixels, containingPix, startPixel.returnAsText());
										for (Pixel p : borderPix) {
											reallyTruePix.remove(pixels[p.x + 1][p.y]);
											reallyTruePix.remove(pixels[p.x - 1][p.y]);
											reallyTruePix.remove(pixels[p.x][p.y + 1]);
											reallyTruePix.remove(pixels[p.x][p.y - 1]);
										}
										edges.innerEdges.add(returnEdgeByDirection(pixels, borderPix));
									}
								}
							}
						}


						return edges;
					}
				}
			}
			byte trueDir = (byte)(nodeDirection.getLast() - 4);
			if (trueDir < 0) trueDir += 8;
			nodeDirection.add((byte)(trueDir));
			dirOffset = nodeDirection.getLast() - 3;
			if (reallyTruePix.contains(eP)) {
				reallyTruePix.remove(eP);
			}
			if (!pixHistory.isEmpty()) {
				historyOffset += 1;
				eP = pixHistory.get(pixHistory.size() - historyOffset);
			} else {
				break outer;
			}
		}
		print(String.valueOf(pixHistory.get(pixHistory.size() - 1).x - startPixel.x));
		print(String.valueOf(pixHistory.get(pixHistory.size() - 1).y - startPixel.y));

		nodeDirection.removeLast();

		if (edges == null) {
			edges = new Edges(startPixel.x, startPixel.y, nodeDirection);
		}

		return edges;
	}

	private NodeList generateNodeFromDirEdge (Pixel[][] pixels, Edges edges) {
		LinkedList<Integer> nodeX = new LinkedList<Integer>();
		LinkedList<Integer> nodeY = new LinkedList<Integer>();

		//TODO ADD SubEdges

		if (edges.innerEdges != null) {
			for (Edges edg : edges.innerEdges) {
				Turtle t = new Turtle(pixels[edg.startX][edg.startY], edg.directions);
				LinkedList<Integer> innerNodeX = t.getNodeX();
				LinkedList<Integer> innerNodeY = t.getNodeY();
				innerNodeX.addLast(innerNodeX.getFirst());
				innerNodeY.addLast(innerNodeY.getFirst());
				Collections.reverse(innerNodeX);
				Collections.reverse(innerNodeY);
				nodeX.addAll(innerNodeX);
				nodeY.addAll(innerNodeY);
			}
		}

		//MainLine
		Turtle t = new Turtle(pixels[edges.startX][edges.startY], edges.directions);
		nodeX.addAll(t.getNodeX());
		nodeY.addAll(t.getNodeY());
		nodeX.addLast(t.getNodeX().getFirst());
		nodeY.addLast(t.getNodeY().getFirst());
		return new NodeList(nodeX, nodeY);
	}

	private NodeList cleanupList (NodeList nl) {

		LinkedList<Integer> nodeX = nl.nodeX;
		LinkedList<Integer> nodeY = nl.nodeY;

		for (int i = 0; i < nodeX.size(); i++) {
			int nextNode = (i + 1) % nodeX.size();
			int lastNode = (i - 1);
			if (lastNode < 0) lastNode += nodeX.size();

			//Last to Cur
			int lastDeltaX = nodeX.get(lastNode) - nodeX.get(i);
			int lastDeltaY = nodeY.get(lastNode) - nodeY.get(i);
			float lastDeltaXn;
			float lastDeltaYn;
			if ((lastDeltaX + lastDeltaY) != 0) {
				lastDeltaXn = lastDeltaX / (lastDeltaX + lastDeltaY);
				lastDeltaYn = lastDeltaY / (lastDeltaX + lastDeltaY);
			} else {
				lastDeltaXn = 0;
				lastDeltaYn = 0;
			}
			//Cur to Next
			int nextDeltaX = nodeX.get(i) - nodeX.get(nextNode);
			int nextDeltaY = nodeY.get(i) - nodeY.get(nextNode);
			float nextDeltaXn;
			float nextDeltaYn;
			if ((nextDeltaX + nextDeltaY) != 0) {
				 nextDeltaXn = nextDeltaX / (nextDeltaX + nextDeltaY);
				nextDeltaYn = nextDeltaY / (nextDeltaX + nextDeltaY);
			} else {
				nextDeltaXn = 0;
				nextDeltaYn = 0;
			}
			if (nextDeltaXn == lastDeltaXn && nextDeltaYn == lastDeltaYn) {
				nodeX.remove(i);
				nodeY.remove(i);
				i--;
			}
		}
		return new NodeList(nodeX, nodeY);
	}
}

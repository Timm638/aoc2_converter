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
		// Direction.debugPrintDirections();

		int y = 0;
		int x = 0;
		while (y < height) {

			if (pixels[x][y] != null) {

				print("Generating Province #" + provinceList.size() + " of Color " + pixels[x][y].returnAsText());
				Province prov = new Province(pixels, this, new Point(x, y));


			} else {
				x++;
				if (x >= width) {
					x = 0;
					y++;
				}
			}
		}

		for (Province prov : provinceList) {
			try {
				prov.exportToFile();
			} catch (IOException e) {
				print(String.format("Failed exporting province id %d: %S", prov.id, e.toString()));
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
				pixels[x][y] = new Pixel(pixelData, new Point(x, y));
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

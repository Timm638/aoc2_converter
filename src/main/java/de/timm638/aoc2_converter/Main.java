package de.timm638.aoc2_converter;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.converters.PathConverter;

public class Main {

	@Parameter(names={"--scale", "-s"}, description = "Modifier to scale image width")
	public int scale = 1;

	@Parameter(converter = FileConverter.class, description = "Path to an image file", required = true)
	File inputImage;
	private int height;
	private int width;

	@Parameter(names={"--output", "-o"}, converter = PathConverter.class, description = "Path to an folder")
	Path outputPath = Paths.get(System.getProperty("user.dir"));

	@Parameter(names={"--verbose", "-v"}, description = "Prints progress to console")
	Boolean verbose = Boolean.FALSE;

	@Parameter(names={"--debug", "-d"}, description = "Outputs debug .svg and other auxillary files")
	Boolean debugOutput = Boolean.FALSE;

	@Parameter(names={"--multiple-province-per-color", "-m"}, description = "Split up disconnected provinces with the same color")
	Boolean splitUpSameColor = Boolean.FALSE;
	// TODO: Implement that

	int[][] provinceMap;

	private void verbosePrint(String str) {
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
		// Ensure output folder exists
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BufferedImage img;
		try {
			img = ImageIO.read(inputImage);
			width = img.getWidth();
			height = img.getHeight();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Pixel[][] pixels = convertImageToArray(img);
		provinceMap = initProvinceMap(img.getWidth(), img.getHeight());

		LinkedList<Province> provinceList = new LinkedList<Province>();
		// Direction.debugPrintDirections();

		int y = 0;
		int x = 0;
		while (y < height) {

			if (provinceMap[x][y] == -1) {
				verbosePrint("Generating Province #" + provinceList.size() + " of Color " + pixels[x][y].returnAsText());
				Province prov = new Province(pixels, this, new Point(x, y));
				provinceList.add(prov);

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
				// export to files
				FileWriter fw = null;
				fw = new FileWriter(outputPath.resolve(Paths.get(String.valueOf(prov.id))).toFile());
				BufferedWriter bw = new BufferedWriter(fw);
				prov.exportToWriter(bw);
				bw.close();
			} catch (IOException e) {
				verbosePrint(String.format("Failed exporting province id %d: %S", prov.id, e.toString()));
			}
		}

		// Export to mapAoC2_v2.txt
		try {
			// export to files
			FileWriter fw = null;
			fw = new FileWriter(outputPath.resolve(Paths.get("mapAoC2_v2.txt")).toFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (Province prov : provinceList) {
				prov.exportToWriter(bw, "\n");
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {
			verbosePrint(String.format("Failed exporting mapAoC2_v2.txt: %S", e.toString()));
		}



		verbosePrint("Finished!");
	}

	private int[][] initProvinceMap(int width, int height) {
		int[][] arr = new int[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				arr[x][y] = -1;
			}
		}
		return arr;
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
}

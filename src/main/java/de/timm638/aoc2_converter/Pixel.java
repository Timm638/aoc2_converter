package de.timm638.aoc2_converter;

public class Pixel implements Comparable<Pixel> {
	public int a = 0;
	public int b;
	public int g;
	public int r;

	final public Point origin;
	public boolean opened;
	public boolean closed;

	public Pixel (int[] arr, Point origin) {
		this.origin = origin;

		if (arr.length > 3) {
			this.a = arr[3];
		}
		this.b = arr[2];
		this.g = arr[1];
		this.r = arr[0];
	}
	
	public String returnAsText () {
		return a + ", " + r + ", " + g + ", " + b;
	}

	@Override
	public int compareTo(Pixel oth) {
		final int aComp = Integer.compare(a, oth.a);
		final int bComp = Integer.compare(b, oth.b);
		final int gComp = Integer.compare(g, oth.g);
		final int rComp = Integer.compare(r, oth.r);
		if (aComp != 0)
			return aComp;
		if (bComp != 0)
			return bComp;
		if (gComp != 0)
			return gComp;
		return rComp;
	}
}

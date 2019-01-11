
public class Pixel {
	public int x;
	public int y;
	
	public byte a;
	public byte b;
	public byte g;
	public byte r;
	
	public boolean opened;
	public boolean closed;
	
	public Pixel (byte a, byte b, byte g, byte r, int x, int y) {
		this.a = a;
		this.b = b;
		this.g = g;
		this.r = r;
		this.x = x;
		this.y = y;
		closed = false;
		opened = false;
	}
	
	public String returnAsText () {
		return a + ", " + r + ", " + g + ", " + b;
	}
	
	public String returnPos () {
		return x + "," + y;
	}
}

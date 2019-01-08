
public class Province {
	static int counter = 0;
	public int id = 0;
	
	public int[] x;
	public int[] y;
	
	public Province (int[] x, int[] y) {
		id = counter;
		counter++;
		
		this.x = x;
		this.y = y;
	}
	
	public static void resetCounter() {
		counter = 0;
	}
}

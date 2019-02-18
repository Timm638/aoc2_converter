import java.util.LinkedList;

public class Edges {
	public int startX;
	public int startY;
	public LinkedList<Byte> directions;
	public LinkedList<Edges> innerEdges;
	
	public Edges (int x, int y, LinkedList<Byte> dir) {
		startX = x;
		startY = y;
		directions = dir;
		innerEdges = null;
	}
}

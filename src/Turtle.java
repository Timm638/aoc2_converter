import java.util.LinkedList;

public class Turtle {
	private int x;
	private int y;
	
	private int startX;
	private int startY;
	
	private LinkedList<Byte> directions;
	
	private byte currectDir;
	private byte lastDir;
	
	private LinkedList<Integer> nodeX;
	private LinkedList<Integer> nodeY;
	public String dirLog;
	
	public Turtle (Pixel startPix, LinkedList<Byte> dir) {
		directions = dir;
		x = startPix.x;
		y = startPix.y;
		startX = x;
		startY = y;
		nodeX = new LinkedList<Integer>();
		nodeY = new LinkedList<Integer>();
		dirLog = "";
		lastDir = -1;
		System.out.println("SP: " + x + "/" + y);
		execute();
		
	}
	
	public void execute () {
		
		for (Byte dir : directions) {
			currectDir = dir;
			forward();
		}
		
		String startPosition = (startX) + "," + (startY);
		String[] posToSearch = {
				(x + 0) + "," + (y - 1),
				(x + 1) + "," + (y - 1),
				(x + 1) + "," + (y + 0),
				(x + 1) + "," + (y + 1),
				(x + 0) + "," + (y + 1),
				(x - 1) + "," + (y + 1),
				(x - 1) + "," + (y + 0),
				(x - 1) + "," + (y - 1),
				};
		
		
		for (byte i = 0; i < 8; i++) {
			if (posToSearch[i].equals(startPosition)) {
				System.out.println("Turtle last tile direction, to Start" + i);
				directions.add(i);
			}
		}
		
		x = startX;
		y = startY;
		
		lastDir = directions.getLast();
		while (!directions.isEmpty()) {
				currectDir = directions.removeFirst();
				dirLog += "[" + lastDir + currectDir + "].";
				placeNode();
				forward();
				lastDir = currectDir;
		}
		
		
		System.out.println("EP: " + x + "/" + y);
		
		
	}
	
	private void forward() {
		switch (currectDir) {
			case 0:y -= 1;break;
			case 2:x += 1;break;
			case 4:y += 1;break;
			case 6:x -= 1;break;
			
			case 1:x += 1;y -= 1;break;
			case 3:x += 1;y += 1;break;
			case 5:x -= 1;y += 1;break;
			case 7:x -= 1;y -= 1;break;
		}
	}
	
	private void placeNode() {
		switch (currectDir) {
		case 7:
		case 0:
			if (lastDir == 4 || lastDir == 5) {
				nodeX.add(x + 1);
				nodeY.add(y + 1);
				dirLog += "[--].";
			}
			if (lastDir == 4 || lastDir == 5 || lastDir == 6 || lastDir == 7) {
				nodeX.add(x);
				nodeY.add(y + 1);
				dirLog += "[--].";
			}
			if (lastDir == 4 || lastDir == 5 || lastDir == 6 || lastDir == 7 || lastDir == 0 || lastDir == 1) {
				nodeX.add(x);
				nodeY.add(y);
			}
		break;
		case 1:
		case 2:
			if (lastDir == 6 || lastDir == 7) {
				nodeX.add(x);
				nodeY.add(y + 1);
				dirLog += "[--].";
			}
			if (lastDir == 6 || lastDir == 7 || lastDir == 0 || lastDir == 1) {
				nodeX.add(x);
				nodeY.add(y);
				dirLog += "[--].";
			}
			if (lastDir == 6 || lastDir == 7 || lastDir == 0 || lastDir == 1 || lastDir == 2 || lastDir == 3) {
				nodeX.add(x + 1);
				nodeY.add(y);
			}
		break;
		case 3:
		case 4:
			if (lastDir == 0 || lastDir == 1) {
				nodeX.add(x);
				nodeY.add(y);
				dirLog += "[--].";
			}
			if (lastDir == 0 || lastDir == 1 || lastDir == 2 || lastDir == 3) {
				nodeX.add(x + 1);
				nodeY.add(y);
				dirLog += "[--].";
			}
			if (lastDir == 0 || lastDir == 1 || lastDir == 2 || lastDir == 3 || lastDir == 4 || lastDir == 5) {
				nodeX.add(x + 1);
				nodeY.add(y + 1);
			}
		break;
		case 5:
		case 6:
			if (lastDir == 2 || lastDir == 3) {
				nodeX.add(x + 1);
				nodeY.add(y);
				dirLog += "[--].";
			}
			if (lastDir == 2 || lastDir == 3 || lastDir == 4 || lastDir == 5) {
				nodeX.add(x + 1);
				nodeY.add(y + 1);
				dirLog += "[--].";
			}
			if (lastDir == 2 || lastDir == 3 || lastDir == 4 || lastDir == 5 || lastDir == 6 || lastDir == 7) {
				nodeX.add(x);
				nodeY.add(y + 1);
			}
		break;
		}
	}
	
	public LinkedList<Integer> getNodeX () {
		return nodeX;
	}
	
	public LinkedList<Integer> getNodeY () {
		return nodeY;
	}
	
}

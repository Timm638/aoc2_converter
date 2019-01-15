import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.w3c.dom.Node;

public class Main {

	public static int SCALE = 1;
	
	public static void main (String[] args) {
		Main m = new Main();
		m.start(args);
	}
	
	public void start (String[] args) {
		Province.resetCounter();
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File(args[0]));
		} catch (IOException e) {
		}
		
		if (args.length > 1) {
			SCALE = Integer.parseInt(args[1]);
		}
		
		
		DataBuffer buffer = img.getRaster().getDataBuffer();
		int width = img.getWidth();
		int height = img.getHeight();
		Pixel[][] pixels = new Pixel[width][height];
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixels[x][y] = new Pixel((byte)buffer.getElem(y * (width * 4) + x * 4), (byte)buffer.getElem(y * (width * 4) + x * 4 + 1), (byte)buffer.getElem(y * (width * 4) + x * 4 + 2), (byte)buffer.getElem(y * (width * 4) + x * 4 + 3), x, y);
			}
			System.out.println("Pixeled Row " + y);
		}
		
		LinkedList<Province> provinceList = new LinkedList<Province>();
		
		boolean running = true;
		int y = 0;
		int x = 0;
		while (running) {
			if (pixels[x][y] != null) {
				
				System.out.println("Start Comapre #" + provinceList.size());
				String compareString = pixels[x][y].returnAsText();
				
				LinkedList<Pixel> closedPixel = new LinkedList<Pixel>();
				LinkedList<Pixel> openPixel = new LinkedList<Pixel>();
				
				pixels[x][y].opened = true;
				openPixel.add(pixels[x][y]);
				while (!openPixel.isEmpty()) {
					Pixel curPixel = openPixel.remove();
					curPixel.opened = false;
					closedPixel.add(curPixel);
					curPixel.closed = true;
					if (curPixel.returnAsText().equals(compareString)) {
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
					if (pix.returnAsText().equals(compareString)) {
						truePix.add(pix);
					}
				}
				
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
				
				String s = "";
				for (Pixel pix : reallyTruePix) {
					s += "[" + pix.x + "," + pix.y + "],";
				}
				System.out.println(s);
				
				if (reallyTruePix.size() < 2) {
					System.out.println("One pixel province detected, please replace it");
					return;
				}
				
				//TODO: Better algorithm to convert to node
				
				LinkedList<Integer> nodeX = new LinkedList<Integer>();
				LinkedList<Integer> nodeY = new LinkedList<Integer>();
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
				byte lastDir = 5;
				
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
							if (px.returnPos().equals(posToSearch[trueDir])) {
								

								lastDir = trueDir;
								nodeDirection.add(trueDir);
								dirOffset = trueDir - 3;
								historyOffset = 0;
								pixHistory.add(eP);
								if (reallyTruePix.contains(eP)) {
								reallyTruePix.remove(eP);
								}
								eP = px;
								continue outer;
							}
						}
					}
					byte trueDir = (byte)(nodeDirection.getLast() - 4);
					if (trueDir < 0) trueDir += 8;
					lastDir = trueDir;
					nodeDirection.add((byte)(trueDir));
					dirOffset = nodeDirection.getLast() - 3;
					if (reallyTruePix.contains(eP)) {
						reallyTruePix.remove(eP);
					}
					if (!pixHistory.isEmpty()) {
						historyOffset += 1;
						if (pixHistory.size() - historyOffset < 0) {
							System.out.println("Enclaved province split province into 2! Color: " + eP.returnAsText());
							return;
						}
						eP = pixHistory.get(pixHistory.size() - historyOffset);
					} else {
						break outer;
					}
				}
				System.out.println(pixHistory.get(pixHistory.size() - 1).x - startPixel.x);
				System.out.println(pixHistory.get(pixHistory.size() - 1).y - startPixel.y);
				
				nodeDirection.removeLast();
				
				String s2 = "";
				for (Byte dir: nodeDirection) {
					s2 += "[" + dir +"],";
				}
				System.out.println(s2);
				
				Turtle t = new Turtle(startPixel, nodeDirection);
				nodeX = t.getNodeX();
				nodeY = t.getNodeY();
				
				String s3 = "";
				for (int dir: nodeX) {
					if (dir < 10) {
						s3 += "[ " + dir +"],";
					} else {
						s3 += "[" + dir +"],";
					}
					
				}
				System.out.println(s3);
				
				String s4 = "";
				for (int dir: nodeY) {
					if (dir < 10) {
						s4 += "[ " + dir +"],";
					} else {
						s4 += "[" + dir +"],";
					}
				}
				System.out.println(s4);
				System.out.println(t.dirLog);
				
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
				
				s3 = "";
				for (int dir: nodeX) {
					if (dir < 10) {
						s3 += "[ " + dir +"],";
					} else {
						s3 += "[" + dir +"],";
					}
					
				}
				System.out.println(s3);
				
				s4 = "";
				for (int dir: nodeY) {
					if (dir < 10) {
						s4 += "[ " + dir +"],";
					} else {
						s4 += "[" + dir +"],";
					}
				}
				System.out.println(s4);
				System.out.println(t.dirLog);
				
				//Managed to get border, using node Direction to create border
				
				//Prepare the Border
				/*
				int cursorX = startPixel.x;
				int cursorY = startPixel.y;
				int lastDir = 0;
				//nodeX.add(cursorX); 
				//nodeY.add(cursorY + 1);
				
				for (int i = 0; i < nodeDirection.size(); i++) {
					switch (nodeDirection.get(i)) {
					case 0: nodeX.add(cursorX); nodeY.add(cursorY + 1); nodeX.add(cursorX); nodeY.add(cursorY); cursorX += 0; cursorY -= 1; break;
					case 1:
						cursorX += 1; cursorY -= 1;  
						break;
					case 2: nodeX.add(cursorX); nodeY.add(cursorY); nodeX.add(cursorX + 1); nodeY.add(cursorY); cursorX += 1; cursorY += 0;  break;
					case 3: nodeX.add(cursorX); nodeY.add(cursorY); nodeX.add(cursorX + 1); nodeY.add(cursorY); cursorX += 1; cursorY += 1;  break;
					case 4: nodeX.add(cursorX + 1); nodeY.add(cursorY); nodeX.add(cursorX + 1); nodeY.add(cursorY + 1); cursorX += 0; cursorY += 1;  break;
					case 5: cursorX -= 1; cursorY += 1; break;
					case 6: nodeX.add(cursorX + 1); nodeY.add(cursorY + 1); nodeX.add(cursorX); nodeY.add(cursorY + 1); cursorX -= 1; cursorY += 0;  break;
					case 7: cursorX -= 1; cursorY -= 1; break;
					}
					lastDir = nodeDirection.get(i);
				}
				*/
				
				
				
				
				
				int[] nodeXArr = new int[nodeX.size()];
				for (int nXi = 0; nXi < nodeX.size(); nXi++) {
					nodeXArr[nXi] = nodeX.get(nXi) * SCALE;
				}
				int[] nodeYArr = new int[nodeY.size()];
				for (int nYi = 0; nYi < nodeY.size(); nYi++) {
					nodeYArr[nYi] = nodeY.get(nYi) * SCALE;
				}
				provinceList.add(new Province(nodeXArr,nodeYArr));
				
				for (Pixel pix : truePix) {
						pixels[pix.x][pix.y] = null;
				}
				for (Pixel pix : closedPixel) {
					pix.closed = false;
				}
				for (Pixel pix : openPixel) {
					pix.opened = false;
				}
				
			} else {
				x++;
				if (x >= width) {
					x = 0;
					y++;
					if (y >= height) {
						running = false;
					}
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
	            System.out.println("AoCC");
	        }
		}
		
		System.out.println("Finished!");
		/*
		for (int y = 0; y < height; y++) {
			String s = y + ": ";
			for (int x = 0; x < width; x++) {
				s += "[" + pixels[x][y].returnAsText() + "], ";
			}
			System.out.println(s);
		} */
	}
}

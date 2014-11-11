package org.ccbr.bader.yeast;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Random;

public class ColorGenerator {

	private LinkedList<Color> baseColors = new LinkedList<Color>();
	{
		baseColors.add(Color.BLUE);
		baseColors.add(Color.RED);       
		baseColors.add(Color.YELLOW);
		baseColors.add(Color.ORANGE);
		baseColors.add(Color.GREEN);
		baseColors.add(Color.MAGENTA);
		baseColors.add(Color.CYAN);
		baseColors.add(Color.PINK);
		baseColors.add(Color.BLACK);
		baseColors.add(Color.GRAY);
	}
	
	private Random random = null;
	
	
	private Random getRandom() {
		if(random == null) {
			random = new Random();
		}
		return random;
	}
	
	private Color getRandomColor() {
		Random gen = getRandom();
        int r = gen.nextInt(256);
        int g = gen.nextInt(256);
        int b = gen.nextInt(256);
        return new Color(r,g,b);
    }
	
	public Color nextColor() {
		if(baseColors.isEmpty())
			return getRandomColor();
		else
			return baseColors.removeFirst();
	}
    
}

package org.ccbr.bader.yeast;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class ColorGenerator {

	private LinkedList<Color> baseColors = new LinkedList<Color>();
	private Random random = null;
	
	public ColorGenerator() {
		Collections.addAll(baseColors, Color.BLUE, Color.RED, Color.YELLOW, Color.ORANGE, Color.GREEN, 
				                       Color.MAGENTA, Color.CYAN, Color.PINK, Color.BLACK, Color.GRAY);
	}
	
	
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

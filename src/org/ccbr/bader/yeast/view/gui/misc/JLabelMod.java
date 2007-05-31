package org.ccbr.bader.yeast.view.gui.misc;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JToolTip;

public class JLabelMod extends JLabel {

	public JLabelMod() {
		// TODO Auto-generated constructor stub
	}

	public JLabelMod(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	public JLabelMod(Icon image) {
		super(image);
		// TODO Auto-generated constructor stub
	}

	public JLabelMod(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public JLabelMod(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public JLabelMod(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	@Override
	public JToolTip createToolTip() {
		return new JMultiLineToolTip();
	}
	
	

}

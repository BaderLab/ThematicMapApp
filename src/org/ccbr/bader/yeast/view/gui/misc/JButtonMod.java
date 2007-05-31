package org.ccbr.bader.yeast.view.gui.misc;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolTip;

public class JButtonMod extends JButton {

	public JButtonMod() {
		super();
	}

	public JButtonMod(Icon arg0) {
		super(arg0);
	}

	public JButtonMod(String arg0) {
		super(arg0);
	}

	public JButtonMod(Action arg0) {
		super(arg0);
	}

	public JButtonMod(String arg0, Icon arg1) {
		super(arg0, arg1);
	}

	@Override
	public JToolTip createToolTip() {
		return new JMultiLineToolTip();
	}
	
}

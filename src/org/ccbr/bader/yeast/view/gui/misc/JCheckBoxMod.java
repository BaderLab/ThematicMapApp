package org.ccbr.bader.yeast.view.gui.misc;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JToolTip;

public class JCheckBoxMod extends JCheckBox {

	

	public JCheckBoxMod() {
		super();
	}

	public JCheckBoxMod(Icon arg0) {
		super(arg0);
	}

	public JCheckBoxMod(String arg0) {
		super(arg0);
	}

	public JCheckBoxMod(Action arg0) {
		super(arg0);
	}

	public JCheckBoxMod(Icon arg0, boolean arg1) {
		super(arg0, arg1);
	}

	public JCheckBoxMod(String arg0, boolean arg1) {
		super(arg0, arg1);
	}

	public JCheckBoxMod(String arg0, Icon arg1) {
		super(arg0, arg1);
	}

	public JCheckBoxMod(String arg0, Icon arg1, boolean arg2) {
		super(arg0, arg1, arg2);
	}

	@Override
	public JToolTip createToolTip() {
		return new JMultiLineToolTip();
	}
	
}

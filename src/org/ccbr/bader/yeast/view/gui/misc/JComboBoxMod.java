package org.ccbr.bader.yeast.view.gui.misc;

import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JToolTip;

public class JComboBoxMod extends JComboBox {

	public JComboBoxMod() {
		// TODO Auto-generated constructor stub
	}

	public JComboBoxMod(ComboBoxModel arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public JComboBoxMod(Object[] arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public JComboBoxMod(Vector<?> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	@Override
	public JToolTip createToolTip() {
		return new JMultiLineToolTip();
	}

}

package org.ccbr.bader.yeast;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class AboutDialogAction extends AbstractCyAction {

	@Inject private CySwingApplication application;
	@Inject private Provider<AboutDialog> dialogProvider;
	
	public AboutDialogAction() {
		super("About Thematic Map...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JDialog aboutDialog = dialogProvider.get();
		aboutDialog.pack();
        aboutDialog.setLocationRelativeTo(application.getJFrame());
        aboutDialog.setVisible(true);
	}

}

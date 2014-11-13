package org.ccbr.bader.yeast;

/**
 * Created by IntelliJ IDEA.
 * User: lmorrison
 * Date: Apr 29, 2008
 * Time: 4:06:23 PM
 * To change this template use File | Settings | File Templates.
 */

import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class CreateThematicMapAction extends AbstractCyAction {

	@Inject private CySwingApplication application;
	@Inject private Provider<CreateThematicMapDialog> dialogProvider;
	
    
    public CreateThematicMapAction() {
		super("Create Thematic Map...");
	}

	@Override
    public void actionPerformed(ActionEvent event) {
		JDialog tmd = dialogProvider.get();
        tmd.setLocationRelativeTo(application.getJFrame());
        tmd.setVisible(true);
    }
}

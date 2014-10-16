package org.ccbr.bader.yeast;

/**
 * Created by IntelliJ IDEA.
 * User: lmorrison
 * Date: Apr 29, 2008
 * Time: 4:06:23 PM
 * To change this template use File | Settings | File Templates.
 */

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ThematicMapPanelAction extends AbstractCyAction {

	@Inject private CySwingApplication application;
	@Inject private Provider<ThematicMapDialog> dialogProvider;
	
    
    public ThematicMapPanelAction() {
		super("Create Thematic Map");
	}

	@Override
    public void actionPerformed(ActionEvent event) {
        ThematicMapDialog tmd = dialogProvider.get();
        tmd.setLocationRelativeTo(application.getJFrame());
        tmd.setVisible(true);
    }
}

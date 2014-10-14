package org.ccbr.bader.yeast;

/**
 * Created by IntelliJ IDEA.
 * User: lmorrison
 * Date: Apr 29, 2008
 * Time: 4:06:23 PM
 * To change this template use File | Settings | File Templates.
 */

import java.awt.event.ActionEvent;
import java.io.IOException;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

@SuppressWarnings("serial")
public class ThematicMapPanelAction extends AbstractCyAction {

	private final CySwingApplication application;
    
    public ThematicMapPanelAction(CySwingApplication application) {
		super("Create Thematic Map");
		this.application = application;
	}

    
	@Override
    public void actionPerformed(ActionEvent event) {

        ThematicMapDialog tmd;

        try {
            tmd = new ThematicMapDialog(application.getJFrame(), true);
            tmd.setLocationRelativeTo(application.getJFrame());
            tmd.setVisible(true);
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }

    }
}

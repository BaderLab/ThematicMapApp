package org.ccbr.bader.yeast;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		CyApplicationManager applicationManager = getService(context, CyApplicationManager.class);
		CySwingApplication application = getService(context, CySwingApplication.class);

		
		
		ThematicMapPanelAction thematicMapAction = new ThematicMapPanelAction(application);
		thematicMapAction.setPreferredMenu("Apps.ThematicMap");
		application.addAction(thematicMapAction);
		
		
	}

}

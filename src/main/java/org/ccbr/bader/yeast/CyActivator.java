package org.ccbr.bader.yeast;

import static org.ops4j.peaberry.Peaberry.osgiModule;
import static org.ops4j.peaberry.Peaberry.service;

import java.awt.Frame;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) {
		
		Injector injector = Guice.createInjector(osgiModule(context), new MainModule());
		
		ThematicMapPanelAction thematicMapAction = injector.getInstance(ThematicMapPanelAction.class);
		thematicMapAction.setPreferredMenu("Apps.ThematicMap");
		registerAllServices(context, thematicMapAction, new Properties());
		
		
	}
	
	
	
	private class MainModule extends AbstractModule {

		@Override
		protected void configure() {
			// Bind Cytoscape services for injection
			bind(CyApplicationManager.class).toProvider(service(CyApplicationManager.class).single());
			bind(CySwingApplication.class).toProvider(service(CySwingApplication.class).single());

		}
		
		@Provides
		protected Frame provideFrame(CySwingApplication application) {
			return application.getJFrame();
		}
	}


}

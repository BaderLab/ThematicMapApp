package org.ccbr.bader.yeast;

import static com.google.inject.name.Names.named;
import static org.ops4j.peaberry.Peaberry.osgiModule;
import static org.ops4j.peaberry.Peaberry.service;
import static org.ops4j.peaberry.util.Filters.ldap;

import java.util.Properties;

import org.ccbr.bader.yeast.statistics.StatisticFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

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
			bind(CyNetworkManager.class).toProvider(service(CyNetworkManager.class).single());
			bind(CyNetworkViewFactory.class).toProvider(service(CyNetworkViewFactory.class).single());
			bind(CyNetworkViewManager.class).toProvider(service(CyNetworkViewManager.class).single());
			bind(CyNetworkFactory.class).toProvider(service(CyNetworkFactory.class).single());
			bind(CyLayoutAlgorithmManager.class).toProvider(service(CyLayoutAlgorithmManager.class).single());
			
			bind(DialogTaskManager.class).toProvider(service(DialogTaskManager.class).single());
			bind(new TypeLiteral<TaskManager<?,?>>(){}).to(DialogTaskManager.class);
			
			bind(VisualMappingManager.class).toProvider(service(VisualMappingManager.class).single());
			bind(VisualStyleFactory.class).toProvider(service(VisualStyleFactory.class).single());
			bind(VisualMappingFunctionFactory.class).annotatedWith(named("continuous")).toProvider(service(VisualMappingFunctionFactory.class).filter(ldap("(mapping.type=continuous)")).single());
			bind(VisualMappingFunctionFactory.class).annotatedWith(named("discrete")).toProvider(service(VisualMappingFunctionFactory.class).filter(ldap("(mapping.type=discrete)")).single());
			bind(VisualMappingFunctionFactory.class).annotatedWith(named("passthrough")).toProvider(service(VisualMappingFunctionFactory.class).filter(ldap("(mapping.type=passthrough)")).single());
			   
			// Bind factories
			install(new FactoryModuleBuilder().build(StatisticFactory.class));
		}
		
	}

}

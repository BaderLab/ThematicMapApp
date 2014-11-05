package org.ccbr.bader.yeast.statistics;

import org.cytoscape.model.CyNetwork;

import com.google.inject.assistedinject.Assisted;

/**
 * Guice factory for creating statistic objects.
 * 
 * @see <a href="http://google.github.io/guice/api-docs/latest/javadoc/com/google/inject/assistedinject/FactoryModuleBuilder.html">FactoryModuleBuilder</a>
 * @author mkucera
 */
public interface StatisticFactory {

	HyperGeomProbabilityStatistic createHyperGeomProbabilityStatistic(@Assisted("originalNetwork") CyNetwork originalNetwork, @Assisted("themeNetwork") CyNetwork themeNetwork);
	
	NetworkShuffleStatistic createNetworkShuffleStatistic(@Assisted("originalNetwork") CyNetwork originalNetwork, @Assisted("themeNetwork") CyNetwork themeNetwork);
	
	NetworkShuffleStatisticMultiThreaded createNetworkShuffleStatisticMultiThreaded(@Assisted("originalNetwork") CyNetwork originalNetwork, @Assisted("themeNetwork") CyNetwork themeNetwork);
	
}

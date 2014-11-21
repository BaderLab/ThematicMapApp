package org.ccbr.bader.yeast;

import java.io.File;
import java.util.List;

import org.ccbr.bader.yeast.statistics.HyperGeomProbabilityStatistic;
import org.ccbr.bader.yeast.statistics.NetworkShuffleStatisticMultiThreaded;
import org.ccbr.bader.yeast.statistics.StatisticFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ThematicMapTask implements Task {

	@Inject private Provider<ThematicMap> thematicMapProvider;
	@Inject private StatisticFactory statisticFactory;
	@Inject private CyApplicationManager applicationManager;
	
	
	public enum StatisticType { NONE, HYPER, HYPER_CULM, SHUFF }
	public enum EdgeWidthType { COUNT, STATISTICS };
	
	private String attributeName;
	private String weightAttributeName;
	private String networkName;
	
	private StatisticType statistic = StatisticType.NONE;
	private EdgeWidthType edgeWidthType = EdgeWidthType.COUNT;
	private boolean removeSelfEdges = false;
	private boolean singleNodes = false;
	
	
	// for shuffle
	private List<Integer> shuffleFlagValues = null;
	private int shuffleIterations;
	private File evaluateFile = null;
	
	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}
	
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public void setWeightAttributeName(String weightAttributeName) {
		this.weightAttributeName = weightAttributeName;
	}
	
	public void setEdgeWidthType(EdgeWidthType type) {
		this.edgeWidthType = type;
	}
	
	public void setStatistic(StatisticType statistic) {
		this.statistic = statistic;
	}
	
	public void setShuffleStatistic(List<Integer> shuffleFlagValues, int shuffleIterations, File evaluateFile) {
		this.statistic = StatisticType.SHUFF;
		this.shuffleFlagValues = shuffleFlagValues;
		this.shuffleIterations = shuffleIterations;
		this.evaluateFile = evaluateFile;
	}
	
	public void setRemoveSelfEdges(boolean removeSelfEdges) {
		this.removeSelfEdges = removeSelfEdges;
	}
	
	public void setSingleNodes(boolean singleNodes) {
		this.singleNodes = singleNodes;
	}
	
	
	
	@Override
	public void run(TaskMonitor monitor) throws Exception {
		monitor.setTitle("Thematic Map");
		monitor.setStatusMessage("Creating Thematic Map For: " + networkName);
		monitor.setProgress(0.1);
		
		ThematicMap tmap = thematicMapProvider.get();
		
        if(weightAttributeName != null) {
        	tmap.setEdgeWeightAttributeName(weightAttributeName);
        }
        tmap.setAllowSelfEdges(!removeSelfEdges);
        
        CyNetwork inputNetwork = applicationManager.getCurrentNetwork();
        CyNetwork thematicMap = tmap.createThematicMap(inputNetwork, attributeName); // running on the UI thread, tsk tsk

        monitor.setProgress(0.4);
        
        // statistics
    	if(StatisticType.HYPER.equals(statistic)) {
    		HyperGeomProbabilityStatistic stat = statisticFactory.createHyperGeomProbabilityStatistic(inputNetwork, thematicMap);
    		stat.getStatistics(attributeName, false);
    	}
    	else if(StatisticType.HYPER_CULM.equals(statistic)) {
    		HyperGeomProbabilityStatistic stat = statisticFactory.createHyperGeomProbabilityStatistic(inputNetwork, thematicMap);
    		stat.getStatistics(attributeName, true);
    	}
    	else if(StatisticType.SHUFF.equals(statistic)) {
            NetworkShuffleStatisticMultiThreaded statThreaded = statisticFactory.createNetworkShuffleStatisticMultiThreaded(inputNetwork, thematicMap);
            statThreaded.getStatistics(attributeName, shuffleIterations, shuffleFlagValues, evaluateFile);
    	}

    	monitor.setProgress(0.8);
    	
        if (singleNodes) {
        	tmap.getSingleNodes(inputNetwork, thematicMap, attributeName);
        }
        
        int widthType = edgeWidthType == EdgeWidthType.COUNT ? ThematicMap.EDGE_WIDTH_COUNT : ThematicMap.EDGE_WIDTH_STATISTICS;
        tmap.createThematicMapDefaultView(thematicMap, attributeName, widthType);
        
        monitor.setProgress(1.0);
	}

	
	@Override
	public void cancel() {
		// not supported
	}


}

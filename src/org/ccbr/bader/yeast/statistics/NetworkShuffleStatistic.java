package org.ccbr.bader.yeast.statistics;

/**
 * Created by IntelliJ IDEA.
 * User: lmorrison
 * Date: Apr 30, 2008
 * Time: 4:58:36 PM
 * To change this template use File | Settings | File Templates.
 */

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Arrays;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import giny.model.Node;
import giny.model.Edge;

import cytoscape.Cytoscape;
import cytoscape.CyNetwork;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.task.TaskMonitor;
import cytoscape.task.Task;
import cytoscape.task.util.TaskManager;
import cytoscape.task.ui.JTaskConfig;

import org.ccbr.bader.yeast.TMUtil;
import org.ccbr.bader.yeast.TM;

public class NetworkShuffleStatistic {

    private CyNetwork originalNetwork;
    private CyNetwork themeNetwork;
    private int trials;
    private String attName;
    private static final CyAttributes edgeAtt = Cytoscape.getEdgeAttributes();
    private static final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();


    public NetworkShuffleStatistic(CyNetwork originalNetwork, CyNetwork themeNetwork) {
        this.originalNetwork = originalNetwork;
        this.themeNetwork = themeNetwork;
    }

    public void getStatisticsProgressBar(String attName, int numTrials) {
        getStatisticsProgressBar(attName, numTrials, null, null);
    }

    public void getStatisticsProgressBar(String attName, int numTrials,  int[] evaluateFlags, File shuffleFile) {
        this.attName = attName;
        trials = numTrials;

        //final String attNameFinal = attName;
        final int[] evaluateFlagsFinal = evaluateFlags;
        final File shuffleFileFinal = shuffleFile;

        JTaskConfig config = new JTaskConfig();
        config.displayStatus(true);
        TaskManager.executeTask(new Task() {

            private TaskMonitor taskMonitor = null;

            public String getTitle() {
                return "Building random networks...";
			}

			public void halt() {
			    // TODO Auto-generated method stub

			}

			public void run() {
                if (taskMonitor == null) {
                    throw new IllegalStateException("Task Monitor is not set.");
                }
                taskMonitor.setPercentCompleted(0);
                getStatistics(evaluateFlagsFinal, shuffleFileFinal, taskMonitor);
            }

			public void setTaskMonitor(TaskMonitor taskMonitor) throws IllegalThreadStateException {
                this.taskMonitor = taskMonitor;
            }

	    }, config);
    }
    
    public void getStatistics(int[] evaluateFlags, File shuffleFile, TaskMonitor taskMonitor) {
        byte attType = TMUtil.getNodeAttType(attName);

        Integer[] finalFlags = null;

        if (evaluateFlags!=null) {

            // Remove any duplicates, flags less than 0, and flags greater than the number of trials, and sort flags in ascending order
            Set<Integer> flagSet = new HashSet<Integer>();
            for (int flag:evaluateFlags) {
                if (flag>0 && flag<=trials) {
                    flagSet.add(flag);
                }
            }

            finalFlags = new Integer[flagSet.size()];

            flagSet.toArray(finalFlags);
            Arrays.sort(finalFlags);

        }

        // loop through edges in the original network, and store sources, targets and types in lists
        int edgeCount = originalNetwork.getEdgeCount();
        List<Node> sourceList = new ArrayList<Node>(edgeCount);
        List<Node> targetList = new ArrayList<Node>(edgeCount);
        List<String> edgeTypeList = new ArrayList<String>(edgeCount);
        Set<String> edgeTypeValues = new HashSet<String>();

        Iterator orig_edges_i = originalNetwork.edgesIterator();
        while (orig_edges_i.hasNext()) {
            Edge edge = (Edge) orig_edges_i.next();

            Node sourceNode = edge.getSource();
            Node targetNode = edge.getTarget();
            String edgeType = edgeAtt.getStringAttribute(edge.getIdentifier(),Semantics.INTERACTION);

            sourceList.add(sourceNode);
            targetList.add(targetNode);
            edgeTypeList.add(edgeType);
            edgeTypeValues.add(edgeType);
        }

        // list of possible values for attribute
        Set<Object> allAttValues = new HashSet<Object>();

        // map of att values associated with each input node
        Map<Node, Set<Object>> nodeToAttValueMap = new HashMap<Node, Set<Object>>();

        // loop through nodes in the original network and store attributes for each node
        Iterator nodes_i = originalNetwork.nodesIterator();
        while (nodes_i.hasNext()) {
            Node node = (Node) nodes_i.next();
            Set<Object> attVals = getAttValues(node, attName, attType);

            // add attribute values for this node to set of all attribute values
            if (attVals != null && !attVals.isEmpty()) {
                allAttValues.addAll(attVals);
            }

            // add this node and its attributes to the map
            nodeToAttValueMap.put(node,attVals);
        }

        //Set<String> edgeTypes = edgeTypeToTargetsMap.keySet();
        List<Object> allAttValuesList = new ArrayList<Object>(allAttValues);
        int numAttributes = allAttValuesList.size();

        // Build a map between the attribute values and their index in the list
        Map<Object, Integer> attValueToIndexMap = new HashMap<Object, Integer>();
        for (int index=0; index<allAttValuesList.size(); index++) {
            attValueToIndexMap.put(allAttValuesList.get(index), index);
        }

        // Create thematic map edge matrices for actual thematic map network
        Map<String,int[][]> edgeTypeToThemeMap = createThemeMapEdgeMatrices(sourceList, targetList, edgeTypeList, nodeToAttValueMap, attValueToIndexMap);

        // Create data structure to hold all randomized maps
        List<Map<String,int[][]>> allRandomizedMaps = new ArrayList<Map<String,int[][]>>(trials);

        int complete;
        int lastComplete = 0;
        int nextFlagIndex = -1;
        List<Map<String, double[][]>> evaluationEdgeTypeToZScoreMapList = null;

        if (finalFlags!=null) {
            nextFlagIndex = 0;
            evaluationEdgeTypeToZScoreMapList = new ArrayList<Map<String, double[][]>>(finalFlags.length);
        }

        for (int i=0; i<trials; i++) {

            if (taskMonitor != null) {
                complete = (int)(((double) i / trials) * 100);
                if ((complete!= lastComplete) && ((complete % 10) == 0)) {
                    lastComplete = complete;
                    taskMonitor.setPercentCompleted(complete);
                }
            }
            
            //randomizeTargets
            List<Node> randomizedTargetList = randomizeList(targetList);

            // create thematic map edge matrices for randomized targets
            Map<String, int[][]> edgeTypeToRandomThemeMap = createThemeMapEdgeMatrices(sourceList, randomizedTargetList, edgeTypeList, nodeToAttValueMap, attValueToIndexMap);
                     
            allRandomizedMaps.add(edgeTypeToRandomThemeMap);

            if (nextFlagIndex>-1 && i==finalFlags[nextFlagIndex]-1) {

                // Calculate z scores and dump to a file
                Map<String, double[][]> zScores = calculateZScores(edgeTypeValues, numAttributes, edgeTypeToThemeMap, allRandomizedMaps);
                evaluationEdgeTypeToZScoreMapList.add(zScores);


                nextFlagIndex++;
                if (nextFlagIndex>=finalFlags.length) {
                    nextFlagIndex = -1;
                }
            }
        }

        // dump evaluation zscores to file
        if (finalFlags!=null) {
            
            try {
                FileWriter fw = new FileWriter(shuffleFile);

                // write headers
                fw.write("Att1Name\tAtt2Name\tEdgeType");

                for (int flag: finalFlags) {
                    fw.write("\tzScore_" + flag);
                }
                fw.write("\n");

                for (String edgeType: edgeTypeValues) {
                    for (int row=0; row<numAttributes; row++) {
                        String rowAttName = allAttValuesList.get(row).toString();
                        for (int col=0; col<numAttributes; col++) {
                            String colAttName = allAttValuesList.get(col).toString();

                            fw.write(rowAttName + "\t" + colAttName + "\t" + edgeType);
                            for (int eachFlag=0; eachFlag<finalFlags.length; eachFlag++) {
                                Map<String, double[][]> zScoresMap = evaluationEdgeTypeToZScoreMapList.get(eachFlag);
                                double[][]zScores = zScoresMap.get(edgeType);
                                fw.write("\t" + zScores[row][col]);
                            }
                            fw.write("\n");
                        }
                    }
                }

                fw.close();               
            }
            catch (IOException e) {
                System.out.println("Problem writing to file....");
            }
        }

        // Calculate averages, standard deviations and z scores
        //Map<String, double[][]> edgeTypeToAverageThemeMap = new HashMap<String, double[][]>();
        //Map<String, double[][]> edgeTypeToStanDevMap = new HashMap<String, double[][]>();

        Map<String, double[][]> edgeTypeToZScoreMap = calculateZScores(edgeTypeValues, numAttributes, edgeTypeToThemeMap, allRandomizedMaps);

        /*
        Map<String, double[][]> edgeTypeToZScoreMap = new HashMap<String, double[][]>();

        for (String type: edgeTypeValues) {

            // Calculate averages
            double[][] averageEdges = new double[numAttributes][numAttributes];

            for (Map<String, int[][]> map:allRandomizedMaps) {
                int[][] edges = map.get(type);
                for (int row=0; row<numAttributes; row++) {
                    for (int col=0; col<numAttributes; col++) {
                        averageEdges[row][col] = averageEdges[row][col] + edges[row][col];
                    }
                }

            }
            for (int row=0; row<numAttributes; row++) {
                for (int col=0; col<numAttributes; col++) {
                    averageEdges[row][col] = averageEdges[row][col]/(trials*1.0);
                }
            }
            //edgeTypeToAverageThemeMap.put(type, averageEdges);

            // Calculate standard deviations
            double[][] stanDevEdges = new double[numAttributes][numAttributes];

            for (Map<String, int[][]> map:allRandomizedMaps) {
                int[][] edges = map.get(type);
                for (int row=0; row<numAttributes; row++) {
                    for (int col=0; col<numAttributes; col++) {
                        stanDevEdges[row][col] = stanDevEdges[row][col] + Math.pow(edges[row][col]-averageEdges[row][col],2);
                    }
                }

            }
            for (int row=0; row<numAttributes; row++) {
                for (int col=0; col<numAttributes; col++) {
                    stanDevEdges[row][col] = Math.sqrt(stanDevEdges[row][col]/(trials*1.0));
                }
            }
            //edgeTypeToStanDevMap.put(type, stanDevEdges);

            // Calculate and store Z scores

            double[][] zScoreEdges = new double[numAttributes][numAttributes];

            int[][] edges = edgeTypeToThemeMap.get(type);

            for (int row=0; row<numAttributes; row++) {
                for (int col=0; col<numAttributes; col++) {
                    zScoreEdges[row][col] = (edges[row][col] - averageEdges[row][col])/stanDevEdges[row][col];
                }
            }

            edgeTypeToZScoreMap.put(type, zScoreEdges);

        }
        */


        // loop through edges in theme map network and add Z score as edge attribute
        Iterator tm_edges_i = themeNetwork.edgesIterator();
        while (tm_edges_i.hasNext()) {
            Edge edge = (Edge) tm_edges_i.next();

            String edgeType = edgeAtt.getStringAttribute(edge.getIdentifier(),Semantics.INTERACTION);
            int index = edgeType.lastIndexOf("_tt");
            String originalEdgeType = edgeType.substring(0,index);

            double[][] zScoreEdges = edgeTypeToZScoreMap.get(originalEdgeType);

            Node source = edge.getSource();
            Node target = edge.getTarget();


            //int sourceIndex = allAttValuesList.indexOf(source.getIdentifier());
            //int targetIndex = allAttValuesList.indexOf(target.getIdentifier());

            int sourceIndex = -1;
            int targetIndex = -1;

            // loop through list of all attributes to find index of source and target attributes
            for (int attIndex=0; attIndex<numAttributes; attIndex++) {
                if (allAttValuesList.get(attIndex).toString().equals(source.getIdentifier())) {
                    sourceIndex = attIndex;
                }
                if (allAttValuesList.get(attIndex).toString().equals(target.getIdentifier())) {
                    targetIndex = attIndex;
                }

                if (sourceIndex!= -1 && targetIndex != -1) {
                    break;
                }
            }

            // ignore directiveness for now
            int row = Math.min(sourceIndex, targetIndex);
            int col = Math.max(sourceIndex, targetIndex);

            double absVal = Math.abs(zScoreEdges[row][col]);
            double roundedVal = (Math.round(absVal*100))/100.0;

            edgeAtt.setAttribute(edge.getIdentifier(),TM.edgeStatisticAttName, roundedVal);
            edgeAtt.setAttribute(edge.getIdentifier(),TM.edgeStatisticTypeAttName, "SHUFFLE");

           
            //edgeAtt.setAttribute(edge.getIdentifier(), "ThemeMapper.Z_SCORE", zScoreEdges[row][col]);
            

        }

    }

    private Set<Object> getAttValues(Node node, String attName, Byte attType) {

        Set<Object> attValues = new HashSet<Object>();

        if (attType == CyAttributes.TYPE_SIMPLE_LIST) {
            List attValList = nodeAtt.getListAttribute(node.getIdentifier(), attName);
            for (Object attVal: attValList) {
                if (attVal!=null) {
                    attValues.add(attVal);
                }
            }
        }
        else {
            Object attVal = null;
            if (attType == CyAttributes.TYPE_STRING) {
                attVal = nodeAtt.getStringAttribute(node.getIdentifier(), attName);
            }
            else if (attType == CyAttributes.TYPE_BOOLEAN) {
                attVal = nodeAtt.getBooleanAttribute(node.getIdentifier(), attName);
            }
            else if (attType == CyAttributes.TYPE_INTEGER) {
                attVal = nodeAtt.getIntegerAttribute(node.getIdentifier(), attName);
            }
            else if (attType == CyAttributes.TYPE_FLOATING) {
                attVal = nodeAtt.getDoubleAttribute(node.getIdentifier(), attName);
            }
            else {
                throw new RuntimeException ("Unsupported theme attribute type '" + Integer.valueOf(attType) + "'.");
            }

            if (attVal != null) {
                attValues.add(attVal);
            }
        }

        return attValues;
    }

    private List<Node> randomizeList(List<Node> originalList) {
        List<Node> randomizedArray = new ArrayList<Node>();
        List<Node> tempList = new ArrayList<Node>(originalList);

        int numElements = originalList.size();

        Random gen = new Random();
        for (int i=0; i<numElements; i++) {
            int random = gen.nextInt(tempList.size());
            randomizedArray.add(tempList.get(random));
            tempList.remove(random);
        }

        return randomizedArray;
    }

    private Map<String, int[][]> createThemeMapEdgeMatrices(List<Node> sources, List<Node> targets, List<String> types, Map<Node, Set<Object>> nodeToAttValueMap, Map<Object, Integer> attValueToIndexMap) {

        int numAttributes = attValueToIndexMap.size();
        Map<String,int[][]> edgeTypeToThemeMap = new HashMap<String, int[][]>();

        // loop through edges
        for (int i = 0; i < sources.size(); i++) {
            Node source = sources.get(i);
            Node target = targets.get(i);
            String type = types.get(i);

            int[][] themeMapEdges;
            if (edgeTypeToThemeMap.containsKey(type)) {
                themeMapEdges = edgeTypeToThemeMap.get(type);
            }
            else {
                themeMapEdges = new int[numAttributes][numAttributes];
            }

            Set<Object> sourceAttributes = nodeToAttValueMap.get(source);
            Set<Object> targetAttributes = nodeToAttValueMap.get(target);

            for (Object sourceAttribute : sourceAttributes) {
                for (Object targetAttribute : targetAttributes) {
                    int sourceIndex = attValueToIndexMap.get(sourceAttribute);
                    int targetIndex = attValueToIndexMap.get(targetAttribute);

                    // ignore directiveness for now
                    int row = Math.min(sourceIndex, targetIndex);
                    int col = Math.max(sourceIndex, targetIndex);
                    themeMapEdges[row][col] = themeMapEdges[row][col] + 1;
                }
            }
            edgeTypeToThemeMap.put(type, themeMapEdges);
        }

        return edgeTypeToThemeMap;

    }

    private Map<String, double[][]> calculateZScores(Set<String> edgeTypeValues, int numAttributes, Map<String,int[][]> edgeTypeToThemeMap, List<Map<String,int[][]>> allRandomizedMaps ) {
        Map<String, double[][]> edgeTypeToZScoreMap = new HashMap<String, double[][]>();

        for (String type: edgeTypeValues) {

            // Calculate averages
            double[][] averageEdges = new double[numAttributes][numAttributes];

            for (Map<String, int[][]> map:allRandomizedMaps) {
                int[][] edges = map.get(type);
                for (int row=0; row<numAttributes; row++) {
                    for (int col=row; col<numAttributes; col++) {
                        averageEdges[row][col] = averageEdges[row][col] + edges[row][col];
                    }
                }

            }
            for (int row=0; row<numAttributes; row++) {
                for (int col=row; col<numAttributes; col++) {
                    averageEdges[row][col] = averageEdges[row][col]/(trials*1.0);
                }
            }
            //edgeTypeToAverageThemeMap.put(type, averageEdges);

            // Calculate standard deviations
            double[][] stanDevEdges = new double[numAttributes][numAttributes];

            for (Map<String, int[][]> map:allRandomizedMaps) {
                int[][] edges = map.get(type);
                for (int row=0; row<numAttributes; row++) {
                    for (int col=row; col<numAttributes; col++) {
                        stanDevEdges[row][col] = stanDevEdges[row][col] + Math.pow(edges[row][col]-averageEdges[row][col],2);
                    }
                }

            }
            for (int row=0; row<numAttributes; row++) {
                for (int col=row; col<numAttributes; col++) {
                    stanDevEdges[row][col] = Math.sqrt(stanDevEdges[row][col]/(trials*1.0));
                }
            }
            //edgeTypeToStanDevMap.put(type, stanDevEdges);

            // Calculate and store Z scores

            double[][] zScoreEdges = new double[numAttributes][numAttributes];

            int[][] edges = edgeTypeToThemeMap.get(type);

            for (int row=0; row<numAttributes; row++) {
                for (int col=row; col<numAttributes; col++) {
                    if (stanDevEdges[row][col] != 0) {
                        zScoreEdges[row][col] = (edges[row][col] - averageEdges[row][col])/stanDevEdges[row][col];
                    }
                    else {
                        zScoreEdges[row][col] = 0;                        
                    }
                }
            }

            edgeTypeToZScoreMap.put(type, zScoreEdges);

        }

        return edgeTypeToZScoreMap;
    }

}

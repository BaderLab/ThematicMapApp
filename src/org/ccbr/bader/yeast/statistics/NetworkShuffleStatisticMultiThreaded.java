package org.ccbr.bader.yeast.statistics;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.ccbr.bader.yeast.TMUtil;
import org.ccbr.bader.yeast.TM;
import giny.model.Node;
import giny.model.Edge;

public class NetworkShuffleStatisticMultiThreaded {

    private CyNetwork originalNetwork;
    private CyNetwork themeNetwork;
    private String attName;
    private int trials;
    private boolean outputToFile;
    private File shuffleDirectory;

    private static final CyAttributes edgeAtt = Cytoscape.getEdgeAttributes();
    private static final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();

    private Integer[] finalFlags;

    private static int NUMPROCESSORS = 8;


    public NetworkShuffleStatisticMultiThreaded(CyNetwork originalNetwork, CyNetwork themeNetwork) {
        this.originalNetwork = originalNetwork;
        this.themeNetwork = themeNetwork;
    }

    public void getStatistics(String attName, int trials, int[] evaluateFlags, File shuffleDirectory) {

        this.attName = attName;
        this.trials = trials;
        this.shuffleDirectory = shuffleDirectory;

        byte attType = TMUtil.getNodeAttType(attName);

        finalFlags = null;

        if (evaluateFlags!=null) {

            outputToFile = true;

            // Remove any duplicates, flags less than 0, and flags greater than the number of trials, and sort flags in ascending order
            Set<Integer> flagSet = new HashSet<Integer>();
            for (int flag:evaluateFlags) {
                if (flag>0 && flag<=trials) {
                    flagSet.add(flag);
                }
            }
            // add final trial as flag as well
            flagSet.add(trials);

            finalFlags = new Integer[flagSet.size()];

            flagSet.toArray(finalFlags);
            Arrays.sort(finalFlags);

        }
        else {

            outputToFile = false;

            finalFlags = new Integer[1];
            finalFlags[0] = trials;
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
            String edgeType = edgeAtt.getStringAttribute(edge.getIdentifier(), Semantics.INTERACTION);

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
            Set<Object> attVals = getAttValues(node, attType);

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
        Map<String,List<int[][]>> allRandomizedMaps2 = new HashMap<String,List<int[][]>>();
        for (String type:edgeTypeValues) {
            List<int[][]> maps = new ArrayList<int[][]>(trials);
            allRandomizedMaps2.put(type,maps);
        }
        List<Map<String,int[][]>> allRandomizedMaps = new ArrayList<Map<String,int[][]>>(trials);


        // determine number of trials for each thread
        int trialsPerThread = trials/NUMPROCESSORS;
        int trialsForLastThread = trialsPerThread + (trials%NUMPROCESSORS);

        //System.out.println(NUMPROCESSORS-1 + " threads with " + trialsPerThread + " trials and 1 thread with " + trialsForLastThread + " trials.");


        Thread[] allThreads = new Thread[NUMPROCESSORS];
        long timeSeed = System.currentTimeMillis();

        for (int i=0; i<NUMPROCESSORS-1; i++) {
            ShuffleThread shuffleThread = new ShuffleThread("Thread"+i,trialsPerThread,timeSeed + i,sourceList,targetList,edgeTypeList,nodeToAttValueMap,attValueToIndexMap,allRandomizedMaps, allRandomizedMaps2);
            Thread thread = new Thread(shuffleThread);
            allThreads[i] = thread;
            thread.start();
        }

        ShuffleThread lastShuffleThread = new ShuffleThread("Thread"+(NUMPROCESSORS-1),trialsForLastThread, timeSeed + NUMPROCESSORS-1,sourceList,targetList,edgeTypeList,nodeToAttValueMap,attValueToIndexMap,allRandomizedMaps, allRandomizedMaps2);
        Thread thread = new Thread(lastShuffleThread);
        allThreads[NUMPROCESSORS-1] = thread;
        thread.start();

        System.out.print("Wait for worker threads to complete\n");
        for (int i=0; i <NUMPROCESSORS; ++i) {
            try {
                allThreads[i].join();
            }
            catch (InterruptedException e) {
                System.out.print("Join interrupted\n");
            }
        }

        // Calculate z scores

        //Map<String, double[][]> edgeTypeToZScoreMap = calculateZScores(edgeTypeValues, numAttributes, edgeTypeToThemeMap, allRandomizedMaps);
        Map<String, double[][]> edgeTypeToZScoreMap = calculateZScoresThreaded(edgeTypeValues, numAttributes, allAttValuesList, edgeTypeToThemeMap, allRandomizedMaps2);


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

            edgeAtt.setAttribute(edge.getIdentifier(), TM.edgeStatisticAttName, roundedVal);
            edgeAtt.setAttribute(edge.getIdentifier(),TM.edgeStatisticTypeAttName, "SHUFFLE");


            //edgeAtt.setAttribute(edge.getIdentifier(), "ThemeMapper.Z_SCORE", zScoreEdges[row][col]);


        }

    }

    private Set<Object> getAttValues(Node node, Byte attType) {

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

    private Map<String, double[][]> calculateZScoresThreaded(Set<String> edgeTypeValues, int numAttributes, List<Object> allAttValuesList, Map<String, int[][]> edgeTypeToThemeMap, Map<String, List<int[][]>> allRandomizedMaps2) {
        Map<String, double[][]> edgeTypeToZScoreMap = new HashMap<String, double[][]>();


        Thread[] lastIterationThreads = new Thread[edgeTypeValues.size()];

        int typeCounter = 0;

        for (String type : edgeTypeValues) {

            int nextFlagIndex = 0;


            // Calculate averages
            double[][] sumEdges = new double[numAttributes][numAttributes];

            double sum_0_0 = 0.0;

            List<int[][]> listOfMaps = allRandomizedMaps2.get(type);

            for (int mapNum = 0; mapNum < listOfMaps.size(); mapNum++) {

                int[][] edges = listOfMaps.get(mapNum);

                for (int row = 0; row < numAttributes; row++) {
                    for (int col = row; col < numAttributes; col++) {
                        if (row==0 && col==0) {
                            sum_0_0 = sum_0_0 + edges[row][col];
                            //System.out.println((mapNum+1) + ": sum_0_0: " + sum_0_0);
                        }
                        sumEdges[row][col] = sumEdges[row][col] + edges[row][col];
                    }
                }

                if (mapNum == finalFlags[nextFlagIndex] - 1) {
                    // create new thread to calculate z scores and print to file

                    double[][] averageEdges = new double[numAttributes][numAttributes];
                    for (int copyRow = 0; copyRow < numAttributes; copyRow++) {
                        for (int copyCol = 0; copyCol < numAttributes; copyCol++) {
                            averageEdges[copyRow][copyCol] = sumEdges[copyRow][copyCol] / (finalFlags[nextFlagIndex] * 1.0);
                        }
                    }

                    if (finalFlags[nextFlagIndex] == trials) {  // last iteration

                        CalcZScoreThread zScoreThread = new CalcZScoreThread(type, finalFlags[nextFlagIndex], numAttributes, allAttValuesList, averageEdges, listOfMaps.subList(0,finalFlags[nextFlagIndex]), edgeTypeToThemeMap.get(type), outputToFile, shuffleDirectory, edgeTypeToZScoreMap);

                        Thread thread = new Thread(zScoreThread);
                        lastIterationThreads[typeCounter] = thread;
                        thread.start();

                    } else {


                        CalcZScoreThread zScoreThread = new CalcZScoreThread(type, finalFlags[nextFlagIndex], numAttributes, allAttValuesList, averageEdges, listOfMaps.subList(0,finalFlags[nextFlagIndex]), edgeTypeToThemeMap.get(type), outputToFile, shuffleDirectory);
                        Thread thread = new Thread(zScoreThread);
                        thread.start();

                    }

                    // use sumEdges, flag number, listOfMaps(0-flagNumber-1)
                    nextFlagIndex++;
                }


            }


            typeCounter++;

        }

        // wait for threads to end

        System.out.print("Wait for calculation threads to complete\n");
        for (int i = 0; i < lastIterationThreads.length; i++) {

            try {
                lastIterationThreads[i].join();
            }
            catch (InterruptedException e) {
                System.out.print("Join interrupted\n");
            }
        }

        return edgeTypeToZScoreMap;


    }


    private Map<String, double[][]> calculateZScores(Set<String> edgeTypeValues, int numAttributes, Map<String, int[][]> edgeTypeToThemeMap, List<Map<String, int[][]>> allRandomizedMaps) {
        Map<String, double[][]> edgeTypeToZScoreMap = new HashMap<String, double[][]>();

        for (String type : edgeTypeValues) {

            // Calculate averages
            double[][] averageEdges = new double[numAttributes][numAttributes];

            for (Map<String, int[][]> map : allRandomizedMaps) {
                int[][] edges = map.get(type);
                for (int row = 0; row < numAttributes; row++) {
                    for (int col = row; col < numAttributes; col++) {
                        averageEdges[row][col] = averageEdges[row][col] + edges[row][col];
                    }
                }

            }
            for (int row = 0; row < numAttributes; row++) {
                for (int col = row; col < numAttributes; col++) {
                    averageEdges[row][col] = averageEdges[row][col] / (trials * 1.0);
                }
            }
            //edgeTypeToAverageThemeMap.put(type, averageEdges);

            // Calculate standard deviations
            double[][] stanDevEdges = new double[numAttributes][numAttributes];

            for (Map<String, int[][]> map : allRandomizedMaps) {
                int[][] edges = map.get(type);
                for (int row = 0; row < numAttributes; row++) {
                    for (int col = row; col < numAttributes; col++) {
                        stanDevEdges[row][col] = stanDevEdges[row][col] + Math.pow(edges[row][col] - averageEdges[row][col], 2);
                    }
                }

            }
            for (int row = 0; row < numAttributes; row++) {
                for (int col = row; col < numAttributes; col++) {
                    stanDevEdges[row][col] = Math.sqrt(stanDevEdges[row][col] / (trials * 1.0));
                }
            }
            //edgeTypeToStanDevMap.put(type, stanDevEdges);

            // Calculate and store Z scores

            double[][] zScoreEdges = new double[numAttributes][numAttributes];

            int[][] edges = edgeTypeToThemeMap.get(type);

            for (int row = 0; row < numAttributes; row++) {
                for (int col = row; col < numAttributes; col++) {
                    if (stanDevEdges[row][col] != 0) {
                        zScoreEdges[row][col] = (edges[row][col] - averageEdges[row][col]) / stanDevEdges[row][col];
                    } else {
                        zScoreEdges[row][col] = 0;
                    }
                }
            }

            edgeTypeToZScoreMap.put(type, zScoreEdges);

        }

        return edgeTypeToZScoreMap;
    }

}

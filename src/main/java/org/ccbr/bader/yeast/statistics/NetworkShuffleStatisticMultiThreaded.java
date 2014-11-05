package org.ccbr.bader.yeast.statistics;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.ccbr.bader.yeast.TM;
import org.ccbr.bader.yeast.TMUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;


public class NetworkShuffleStatisticMultiThreaded {

    private final CyNetwork originalNetwork;
    private final CyNetwork themeNetwork;
    
    private String attName;
    private int trials;
    private boolean outputToFile;
    private File shuffleDirectory;

    private Integer[] finalFlags;

    private static int NUMPROCESSORS = 8;


    @Inject
    public NetworkShuffleStatisticMultiThreaded(@Assisted("originalNetwork") CyNetwork originalNetwork, @Assisted("themeNetwork") CyNetwork themeNetwork) {
        this.originalNetwork = originalNetwork;
        this.themeNetwork = themeNetwork;
    }

    public void getStatistics(String attName, int trials, int[] evaluateFlags, File shuffleDirectory) {

        this.attName = attName;
        this.trials = trials;
        this.shuffleDirectory = shuffleDirectory;

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
        List<CyNode> sourceList = new ArrayList<CyNode>(edgeCount);
        List<CyNode> targetList = new ArrayList<CyNode>(edgeCount);
        List<String> edgeTypeList = new ArrayList<String>(edgeCount);
        Set<String> edgeTypeValues = new HashSet<String>();

        for(CyEdge edge : originalNetwork.getEdgeList()) {
            CyNode sourceNode = edge.getSource();
            CyNode targetNode = edge.getTarget();
            
            String edgeType = themeNetwork.getRow(edge).get(CyEdge.INTERACTION, String.class);

            sourceList.add(sourceNode);
            targetList.add(targetNode);
            edgeTypeList.add(edgeType);
            edgeTypeValues.add(edgeType);
        }

        // list of possible values for attribute
        Set<Object> allAttValues = new HashSet<Object>();

        // map of att values associated with each input node
        Map<CyNode, Collection<?>> nodeToAttValueMap = new HashMap<CyNode, Collection<?>>();

        // loop through nodes in the original network and store attributes for each node
        for(CyNode node : originalNetwork.getNodeList()) {
            Collection<?> attVals = TMUtil.getAttValues(originalNetwork, node, attName);

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
        for(CyEdge edge : themeNetwork.getEdgeList()) {
            String edgeType = themeNetwork.getRow(edge).get(CyEdge.INTERACTION, String.class);
            int index = edgeType.lastIndexOf("_tt");
            String originalEdgeType = edgeType.substring(0,index);

            double[][] zScoreEdges = edgeTypeToZScoreMap.get(originalEdgeType);

            CyNode source = edge.getSource();
            CyNode target = edge.getTarget();


            //int sourceIndex = allAttValuesList.indexOf(source.getIdentifier());
            //int targetIndex = allAttValuesList.indexOf(target.getIdentifier());

            int sourceIndex = -1;
            int targetIndex = -1;

            // loop through list of all attributes to find index of source and target attributes
            for (int attIndex=0; attIndex<numAttributes; attIndex++) {
                if (allAttValuesList.get(attIndex).toString().equals(source.getSUID().toString())) {
                    sourceIndex = attIndex;
                }
                if (allAttValuesList.get(attIndex).toString().equals(target.getSUID().toString())) {
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

            themeNetwork.getRow(edge).set(TM.edgeStatisticAttName, roundedVal);
            themeNetwork.getRow(edge).set(TM.edgeStatisticTypeAttName, "SHUFFLE");

            //edgeAtt.setAttribute(edge.getIdentifier(), "ThemeMapper.Z_SCORE", zScoreEdges[row][col]);


        }

    }


    private List<CyNode> randomizeList(List<CyNode> originalList) {
        List<CyNode> randomizedArray = new ArrayList<CyNode>();
        List<CyNode> tempList = new ArrayList<CyNode>(originalList);

        int numElements = originalList.size();

        Random gen = new Random();
        for (int i=0; i<numElements; i++) {
            int random = gen.nextInt(tempList.size());
            randomizedArray.add(tempList.get(random));
            tempList.remove(random);
        }

        return randomizedArray;
    }

    private Map<String, int[][]> createThemeMapEdgeMatrices(List<CyNode> sources, List<CyNode> targets, List<String> types, Map<CyNode, Collection<?>> nodeToAttValueMap, Map<Object, Integer> attValueToIndexMap) {

        int numAttributes = attValueToIndexMap.size();
        Map<String,int[][]> edgeTypeToThemeMap = new HashMap<String, int[][]>();

        // loop through edges
        for (int i = 0; i < sources.size(); i++) {
        	CyNode source = sources.get(i);
        	CyNode target = targets.get(i);
            String type = types.get(i);

            int[][] themeMapEdges;
            if (edgeTypeToThemeMap.containsKey(type)) {
                themeMapEdges = edgeTypeToThemeMap.get(type);
            }
            else {
                themeMapEdges = new int[numAttributes][numAttributes];
            }

            Collection<?> sourceAttributes = nodeToAttValueMap.get(source);
            Collection<?> targetAttributes = nodeToAttValueMap.get(target);

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

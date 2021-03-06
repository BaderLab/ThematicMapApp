package org.ccbr.bader.yeast.statistics;

/**
 * Created by IntelliJ IDEA.
 * User: lmorrison
 * Date: Jul 18, 2008
 * Time: 1:44:07 PM
 * To change this template use File | Settings | File Templates.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cytoscape.model.CyNode;


public class ShuffleThread implements Runnable {

    List<CyNode> sourceList;
    List<CyNode> targetList;
    List<String> edgeTypeList;
    Map<CyNode, Collection<?>> nodeToAttValueMap;       // map of att values associated with each input node
    Map<Object, Integer> attValueToIndexMap;  // map of attribute values and their index in the list
    Random gen;

    // List of all randomized maps - object to be synchronized between threads
    final List<Map<String,int[][]>> allRandomizedMaps;
    final Map<String,List<int[][]>> allRandomizedMaps2;

    int trials;
    String name;

    public ShuffleThread(String name, int trials, long generatorSeed, List<CyNode> sourceList, List<CyNode> targetList, List<String> edgeTypeList, Map<CyNode, Collection<?>> nodeToAttValueMap, Map<Object, Integer> attValueToIndexMap, List<Map<String,int[][]>> allRandomizedMaps, Map<String,List<int[][]>> allRandomizedMaps2) {
        this.trials = trials;
        this.name = name;

        this.sourceList = sourceList;
        this.targetList = targetList;
        this.edgeTypeList = edgeTypeList;
        this.nodeToAttValueMap = nodeToAttValueMap;
        this.attValueToIndexMap = attValueToIndexMap;
        this.allRandomizedMaps = allRandomizedMaps;
        this.allRandomizedMaps2 = allRandomizedMaps2;

        gen = new Random(generatorSeed);
    }

    // override run() method in interface
    public void run() {


        for(int i=0; i<trials; i++) {

            //randomizeTargets
            List<CyNode> randomizedTargetList = randomizeList(targetList);

            // create thematic map edge matrices for randomized targets
            Map<String, int[][]> edgeTypeToRandomThemeMap = createThemeMapEdgeMatrices(sourceList, randomizedTargetList, edgeTypeList, nodeToAttValueMap, attValueToIndexMap);

            /*
            synchronized (allRandomizedMaps) {
                //System.out.println("thread: " + name + "trial: " + i);
                allRandomizedMaps.add(edgeTypeToRandomThemeMap);
            }
            */
            synchronized(allRandomizedMaps2) {
                for (String type:edgeTypeToRandomThemeMap.keySet()) {
                    List<int[][]> list = allRandomizedMaps2.get(type);
                    list.add(edgeTypeToRandomThemeMap.get(type));
                    allRandomizedMaps2.put(type,list);
                }
            }
            /*
            try{
                //Thread.sleep((int)(Math.random() * 10));
                Thread.sleep(3);
            } catch( InterruptedException e ) {
                System.out.println("Interrupted Exception caught");
            }
            */
        }
    }

    private List<CyNode> randomizeList(List<CyNode> originalList) {
        List<CyNode> randomizedArray = new ArrayList<CyNode>();
        List<CyNode> tempList = new ArrayList<CyNode>(originalList);

        int numElements = originalList.size();

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

}

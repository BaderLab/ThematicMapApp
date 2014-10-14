package org.ccbr.bader.yeast.statistics;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CalcZScoreThread implements Runnable {

    int flagNumber;
    String edgeType;
    int numAttributes;
    List<Object> allAttValuesList;
    double[][] averageEdges;
    List<int[][]> listOfMaps;
    int[][] originalMap;
    boolean outputToFile;
    File shuffleDirectory;

    final Map<String, double[][]> edgeTypeToZScoreMap;

    public CalcZScoreThread(String type, int flagNumber, int numAttributes, List<Object> allAttValuesList, double[][] averageEdges, List<int[][]> listOfMaps, int[][] originalMap, boolean outputToFile, File shuffleDirectory, Map<String, double[][]> edgeTypeToZScoreMap) {

        this.edgeType = type;
        this.flagNumber = flagNumber;
        this.numAttributes = numAttributes;
        this.allAttValuesList = allAttValuesList;
        this.averageEdges = averageEdges;
        this.listOfMaps = listOfMaps;
        this.originalMap = originalMap;
        this.outputToFile = outputToFile;
        this.shuffleDirectory = shuffleDirectory;

        this.edgeTypeToZScoreMap = edgeTypeToZScoreMap;
    }

    public CalcZScoreThread(String type, int flagNumber, int numAttributes, List<Object> allAttValuesList, double[][] averageEdges, List<int[][]> listOfMaps, int[][] originalMap, boolean outputToFile, File shuffleDirectory) {

        this.edgeType = type;
        this.flagNumber = flagNumber;
        this.numAttributes = numAttributes;
        this.allAttValuesList = allAttValuesList;
        this.averageEdges = averageEdges;
        this.listOfMaps = listOfMaps;
        this.originalMap = originalMap;
        this.outputToFile = outputToFile;
        this.shuffleDirectory = shuffleDirectory;

        this.edgeTypeToZScoreMap = null;
        
    }

    // override run() method in interface
    public void run() {

        // Calculate standard deviations
        double[][] stanDevEdges = new double[numAttributes][numAttributes];

        double[] temp = new double[listOfMaps.size()];

        for (int[][] edges:listOfMaps) {
            for (int row=0; row<numAttributes; row++) {
                for (int col=row; col<numAttributes; col++) {
                    stanDevEdges[row][col] = stanDevEdges[row][col] + Math.pow(edges[row][col]-averageEdges[row][col],2);
                }
            }

        }

        for (int row=0; row<numAttributes; row++) {
            for (int col=row; col<numAttributes; col++) {
                stanDevEdges[row][col] = Math.sqrt(stanDevEdges[row][col]/(flagNumber*1.0));
            }
        }

        // Calculate and store Z scores

        double[][] zScoreEdges = new double[numAttributes][numAttributes];

        for (int row=0; row<numAttributes; row++) {
            for (int col=row; col<numAttributes; col++) {
                if (stanDevEdges[row][col] != 0) {
                    zScoreEdges[row][col] = (originalMap[row][col] - averageEdges[row][col])/stanDevEdges[row][col];
                }
                else {
                    zScoreEdges[row][col] = 0;
                }
            }
        }

        if (edgeTypeToZScoreMap != null) {
            synchronized (edgeTypeToZScoreMap) {
                //System.out.println("thread: " + name + "trial: " + i);
                edgeTypeToZScoreMap.put(edgeType,zScoreEdges);
            }
        }

        if (outputToFile) {
            // output to file here...
            //System.out.println("output to file: " + shuffleDirectory + ":" + edgeType + "_" + flagNumber);
            File outputFile = new File (shuffleDirectory,"shuffleOuput_" + edgeType + "_" + flagNumber + ".txt");
            //System.out.println(outputFile.getAbsolutePath());


            try {
                FileWriter fw = new FileWriter(outputFile);

                // write headers
                fw.write("Att1Name\tAtt2Name\tZScore\n");

                for (int row = 0; row < numAttributes; row++) {
                    String rowAttName = allAttValuesList.get(row).toString();
                    for (int col = 0; col < numAttributes; col++) {
                        String colAttName = allAttValuesList.get(col).toString();

                        fw.write(rowAttName + "\t" + colAttName);
                        fw.write("\t" + zScoreEdges[row][col]);

                        fw.write("\n");
                    }
                }


                fw.close();
            }
            catch (IOException e) {
                System.out.println("Problem writing to file....");
            }
            


        }
       
        
    }



}

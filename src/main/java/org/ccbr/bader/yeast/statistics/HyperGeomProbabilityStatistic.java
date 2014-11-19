package org.ccbr.bader.yeast.statistics;

import java.util.Collection;

import org.ccbr.bader.yeast.TM;
import org.ccbr.bader.yeast.TMUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import cern.jet.stat.Gamma;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class HyperGeomProbabilityStatistic {

    private final CyNetwork originalNetwork;
    private final CyNetwork themeNetwork;


    @Inject
    public HyperGeomProbabilityStatistic(@Assisted("originalNetwork") CyNetwork originalNetwork, @Assisted("themeNetwork") CyNetwork themeNetwork) {
        this.originalNetwork = originalNetwork;
        this.themeNetwork = themeNetwork;
    }

    public void getStatistics(String attName, boolean cumulative) {
        int nodeCount = originalNetwork.getNodeCount();

        // loop through each edge in the thematic map, and for each edge, calculate the probability
        for(CyEdge themeMapEdge : themeNetwork.getEdgeList()) {

            // get edge type from theme map edge type
            String themeEdgeType = themeNetwork.getRow(themeMapEdge).get(CyEdge.INTERACTION, String.class);
            
            int index = themeEdgeType.lastIndexOf("_tt");
            String originalEdgeType = themeEdgeType.substring(0,index);


            CyNode themeSource = themeMapEdge.getSource();
            CyNode themeTarget = themeMapEdge.getTarget();

            String att1 = themeNetwork.getRow(themeSource).get(CyNetwork.NAME, String.class);
            String att2 = themeNetwork.getRow(themeTarget).get(CyNetwork.NAME, String.class);
//            Long att1 = themeSource.getSUID(); //getIdentifier();
//            Long att2 = themeTarget.getSUID(); //getIdentifier();

            //byte attType = TMUtil.getNodeAttType(attName);

            // Loop through nodes in the original network, and determine how many nodes have attribute 1,
            // how many have att2 and how many have both.
            int countAtt1 = 0;
            int countAtt2 = 0;
            int countAtt1and2 = 0;

            for(CyNode node : originalNetwork.getNodeList()) {
                Collection<?> attVals = TMUtil.getAttValues(originalNetwork, node, attName);

                boolean hasAtt1 = false;
                boolean hasAtt2 = false;

                for(Object val : attVals) {
                    if (val.toString().equals(att1)) {
                        hasAtt1 = true;
                    }
                    if (val.toString().equals(att2)) {
                        hasAtt2 = true;
                    }
                    if (hasAtt1 && hasAtt2) {
                        break;
                    }
                }

                if (hasAtt1) {
                    countAtt1++;
                }
                if (hasAtt2) {
                    countAtt2++;
                }
                if (hasAtt1 && hasAtt2) {
                    countAtt1and2++;
                }

            }

            // Calculate N = total number of possible edges
            //System.out.println("counts -- att1: " + countAtt1 + ", att2: " + countAtt2 + ", att1&2: " + countAtt1and2);

            // Calculate number of nodes in modified network
            int n = nodeCount + countAtt1and2;

            // Calculate total number of edges possible
            int N = ((n * (n-1)) / 2) - countAtt1and2;

            // Calculate k = number of possible Attribute1-Attribute2 edges
            int k = (countAtt1 * countAtt2) - countAtt1and2;

            // Loop through the edges in the original network to determine the number of edges and the number of
            // Attribute 1 - Attribute 2 edges for each edge type

            int totalEdges = 0;
            int att1att2Edges = 0;

            for(CyEdge edge : originalNetwork.getEdgeList()) {
            	String edgeType = originalNetwork.getRow(edge).get(CyEdge.INTERACTION, String.class);
                if (!edgeType.equals(originalEdgeType)) {
                    continue;
                }

                CyNode source = edge.getSource();
                CyNode target = edge.getTarget();

                Collection<?> sourceAttVals = TMUtil.getAttValues(originalNetwork, source, attName);
                Collection<?> targetAttVals = TMUtil.getAttValues(originalNetwork, target, attName);

                boolean sourceHasAtt1 = false;
                boolean sourceHasAtt2 = false;

                for(Object val : sourceAttVals) {
                    if (val.toString().equals(att1)) {
                        sourceHasAtt1 = true;
                    }
                    if (val.toString().equals(att2)) {
                        sourceHasAtt2 = true;
                    }
                    if (sourceHasAtt1 && sourceHasAtt2) {
                        break;
                    }
                }

                boolean targetHasAtt1 = false;
                boolean targetHasAtt2 = false;

                for(Object val : targetAttVals) {
                    if (val.toString().equals(att1)) {
                        targetHasAtt1 = true;
                    }
                    if (val.toString().equals(att2)) {
                        targetHasAtt2 = true;
                    }
                    if (targetHasAtt1 && targetHasAtt2) {
                        break;
                    }
                }

                boolean sourceHasBoth = sourceHasAtt1 && sourceHasAtt2;
                boolean targetHasBoth = targetHasAtt1 && targetHasAtt2;

                // add appropriate number to edge count
                if (sourceHasBoth && targetHasBoth) {
                    totalEdges = totalEdges + 4;
                }
                else if (sourceHasBoth || targetHasBoth) {
                    totalEdges = totalEdges + 2;
                }
                else {
                    totalEdges = totalEdges + 1;
                }

                // add appropriate number to att1-att2 edge count
                if (sourceHasBoth && targetHasBoth) {
                    att1att2Edges = att1att2Edges + 2;
                }
                else if (sourceHasBoth && (targetHasAtt1 || targetHasAtt2)) {
                    att1att2Edges = att1att2Edges + 1;
                }
                else if (targetHasBoth && (sourceHasAtt1 || sourceHasAtt2)) {
                    att1att2Edges = att1att2Edges + 1;
                }
                else if ((sourceHasAtt1 && targetHasAtt2) || (sourceHasAtt2 && targetHasAtt1)) {
                    att1att2Edges = att1att2Edges + 1;
                }
                // otherwise, don't increase count
            }

            double probability;
            if (cumulative) {
                probability = 1 - calculateCumulativeHypergDistr(att1att2Edges, totalEdges, k, N);
                themeNetwork.getRow(themeMapEdge).set(TM.edgeStatisticTypeAttName.name, "CUMULATIVE HYPERGEOMETRIC");
            }
            else {
                probability = 1 - calculateHypergDistr(att1att2Edges, totalEdges, k, N);
                themeNetwork.getRow(themeMapEdge).set(TM.edgeStatisticTypeAttName.name, "HYPERGEOMETRIC");

            }
            double roundedVal = (Math.round(probability*100))/100.0;

            themeNetwork.getRow(themeMapEdge).set(TM.edgeStatisticAttName.name, roundedVal);
        }

    }


    /**
     * method that conducts the calculations.
     * P(x or more |X,N,n) = 1 - sum{[C(n,i)*C(N-n, X-i)] / C(N,X)}
     * for i=0 ... x-1
     *
     * @param x    number of attribute1-attribute 2 edges in the network.
     * @param m    number of edges in the network
     * @param k    number of possible attribute1-attribute2 edges.
     * @param N    number of possible edges
     * @return double with result of calculations.
     */
    private static double calculateCumulativeHypergDistr(int x, int m, int k, int N) {
        //double dist = calculateHypergDistr(att1att2Edges,totalEdges,possibleAtt1Att2Edges,possibleEdges);

        if (N >= 2) {
            double sum = 0;
            //mode of distribution, integer division (returns integer <= double result)!
            int mode = (m + 1) * (k + 1) / (N + 2);
            if (x >= mode) {
                int i = x;
                while ((N - k >= m - i) && (i <= Math.min(m, k))) {
                    double pdfi = Math.exp(Gamma.logGamma(k + 1) - Gamma.logGamma(i + 1) - Gamma.logGamma(k - i + 1) + Gamma.logGamma(N - k + 1) - Gamma.logGamma(m - i + 1) - Gamma.logGamma(N - k - m + i + 1) - Gamma.logGamma(N + 1) + Gamma.logGamma(m + 1) + Gamma.logGamma(N - m + 1));
                    sum = sum + pdfi;
                    i++;
                }
            }
            else {
                int i = x - 1;
                while ((N - k >= m - i) && (i >= 0)) {
                    double pdfi = Math.exp(Gamma.logGamma(k + 1) - Gamma.logGamma(i + 1) - Gamma.logGamma(k - i + 1) + Gamma.logGamma(N - k + 1) - Gamma.logGamma(m - i + 1) - Gamma.logGamma(N - k - m + i + 1) - Gamma.logGamma(N + 1) + Gamma.logGamma(m + 1) + Gamma.logGamma(N - m + 1));
                    sum = sum + pdfi;
                    i--;
                }
                sum = 1 - sum;
            }
            //return (new Double(sum)).toString();
            return sum;
        }
        else {
            //return (new Double(1)).toString();
            return 1.0;
        }
    }
    private static double calculateHypergDistr(int x, int m, int k, int N) {
        //double dist = calculateHypergDistr(att1att2Edges,totalEdges,possibleAtt1Att2Edges,possibleEdges);

        double prob = 0.0;
        if (((N-k) >= m-x) && (x <= Math.min(m,k)) && (x >= 0)) {
            prob = Math.exp(Gamma.logGamma(k + 1) - Gamma.logGamma(x + 1) - Gamma.logGamma(k - x + 1) + Gamma.logGamma(N - k + 1) - Gamma.logGamma(m - x + 1) - Gamma.logGamma(N - k - m + x + 1) - Gamma.logGamma(N + 1) + Gamma.logGamma(m + 1) + Gamma.logGamma(N - m + 1));
        }
        return prob;
    }

}

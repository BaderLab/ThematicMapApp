package org.ccbr.bader.yeast.statistics;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import org.ccbr.bader.yeast.TMUtil;
import org.ccbr.bader.yeast.TM;

import java.util.*;

import java.math.BigDecimal;

import giny.model.Node;
import giny.model.Edge;

import cern.jet.stat.*;
import java.math.*;

public class HyperGeomProbabilityStatistic {

    private CyNetwork originalNetwork;
    private CyNetwork themeNetwork;

    private static final CyAttributes edgeAtt = Cytoscape.getEdgeAttributes();
    private static final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();


    public HyperGeomProbabilityStatistic(CyNetwork originalNetwork, CyNetwork themeNetwork) {
        this.originalNetwork = originalNetwork;
        this.themeNetwork = themeNetwork;
    }

    public void getStatistics(String attName, boolean cumulative) {

        // loop through each edge in the thematic map, and for each edge, calculate the probability
        Iterator themeMapEdges_i = themeNetwork.edgesIterator();

        int nodeCount = originalNetwork.getNodeCount();

        while (themeMapEdges_i.hasNext()) {
            Edge themeMapEdge = (Edge) themeMapEdges_i.next();

            // get edge type from theme map edge type
            String themeEdgeType = edgeAtt.getStringAttribute(themeMapEdge.getIdentifier(),Semantics.INTERACTION);
            int index = themeEdgeType.lastIndexOf("_tt");
            String originalEdgeType = themeEdgeType.substring(0,index);


            Node themeSource = themeMapEdge.getSource();
            Node themeTarget = themeMapEdge.getTarget();

            String att1 = themeSource.getIdentifier();
            String att2 = themeTarget.getIdentifier();

            byte attType = TMUtil.getNodeAttType(attName);

            // Loop through nodes in the original network, and determine how many nodes have attribute 1,
            // how many have att2 and how many have both.
            int countAtt1 = 0;
            int countAtt2 = 0;
            int countAtt1and2 = 0;

            Iterator nodes_i = originalNetwork.nodesIterator();
            while (nodes_i.hasNext()) {
                Node node = (Node) nodes_i.next();

                Set<Object> attVals = getAttValues(node, attName, attType);

                boolean hasAtt1 = false;
                boolean hasAtt2 = false;

                Iterator<Object> valsIt = attVals.iterator();
                while (valsIt.hasNext()) {
                    Object val = valsIt.next();
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

            Iterator edges_i = originalNetwork.edgesIterator();
            while (edges_i.hasNext()) {
                Edge edge = (Edge) edges_i.next();

                String edgeType = edgeAtt.getStringAttribute(edge.getIdentifier(),Semantics.INTERACTION);
                if (!edgeType.equals(originalEdgeType)) {
                    continue;
                }

                Node source = edge.getSource();
                Node target = edge.getTarget();

                Set<Object> sourceAttVals = getAttValues(source, attName, attType);
                Set<Object> targetAttVals = getAttValues(target, attName, attType);

                boolean sourceHasAtt1 = false;
                boolean sourceHasAtt2 = false;

                Iterator<Object> sourceValsIt = sourceAttVals.iterator();
                while (sourceValsIt.hasNext()) {
                    Object val = sourceValsIt.next();
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

                Iterator<Object> targetValsIt = targetAttVals.iterator();
                while (targetValsIt.hasNext()) {
                    Object val = targetValsIt.next();
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
                edgeAtt.setAttribute(themeMapEdge.getIdentifier(), TM.edgeStatisticTypeAttName, "CUMULATIVE HYPERGEOMETRIC");
            }
            else {
                probability = 1 - calculateHypergDistr(att1att2Edges, totalEdges, k, N);
                edgeAtt.setAttribute(themeMapEdge.getIdentifier(), TM.edgeStatisticTypeAttName, "HYPERGEOMETRIC");

            }
            double roundedVal = (Math.round(probability*100))/100.0;

            edgeAtt.setAttribute(themeMapEdge.getIdentifier(),TM.edgeStatisticAttName, roundedVal);

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
    public double calculateCumulativeHypergDistr(int x, int m, int k, int N) {
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
    public double calculateHypergDistr(int x, int m, int k, int N) {
        //double dist = calculateHypergDistr(att1att2Edges,totalEdges,possibleAtt1Att2Edges,possibleEdges);

        double prob = 0.0;
        if (((N-k) >= m-x) && (x <= Math.min(m,k)) && (x >= 0)) {
            prob = Math.exp(Gamma.logGamma(k + 1) - Gamma.logGamma(x + 1) - Gamma.logGamma(k - x + 1) + Gamma.logGamma(N - k + 1) - Gamma.logGamma(m - x + 1) - Gamma.logGamma(N - k - m + x + 1) - Gamma.logGamma(N + 1) + Gamma.logGamma(m + 1) + Gamma.logGamma(N - m + 1));
        }
        return prob;
    }

}

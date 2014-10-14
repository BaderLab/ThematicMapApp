/**
 * * Copyright (c) 2007 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 * * Research, University of Toronto
 * *
 * * Code written by: Michael Matan
 * * Authors: Michael Matan, Gary D. Bader
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * University of Toronto
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * University of Toronto
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * University of Toronto
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * *
 * * Description: Provides a number of utility methods useful by other parts of the plugin  
 */
package org.ccbr.bader.yeast;

import java.util.ArrayList;
import java.util.List;

import giny.model.Edge;
import giny.model.Node;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

/**Provides a number of utility methods useful by other parts of the plugin
 * @author mikematan
 *
 */
public class TMUtil {
	
	private static final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
	private static final CyAttributes edgeAtt = Cytoscape.getEdgeAttributes();
	private static final CyAttributes netAtt = Cytoscape.getNetworkAttributes();
	
	public static String getStringAtt(Node node, String attName) {
		return nodeAtt.getStringAttribute(node.getIdentifier(), attName);
	}
	
	public static byte getNodeAttType(String attName) {
		return nodeAtt.getType(attName);
	}



	public static List<String> getStringListAtt(Node node, String attName) {
		//TODO consider placing a check to make sure it is a string list
		return nodeAtt.getListAttribute(node.getIdentifier(), attName);
	}

	public static void setStringAtt(Node node, String attributeName, String value) {
		nodeAtt.setAttribute(node.getIdentifier(), attributeName, value);
	}
	
	public static void setStringAtt(Edge edge, String attributeName, String value) {
		edgeAtt.setAttribute(edge.getIdentifier(), attributeName, value);
	}
	
	/**Adds the value parameter to the given node's specified list attribute, creating the attribute if it does not already exist 
	 * @param node the node for whom the list attribute should be appended to
	 * @param attributeName the name of the list attribute to be appended
	 * @param value the value to append to the list
	 */
	public static void addToListAttribute(Node node, String attributeName, String value) {
		addToListAttribute(nodeAtt, node.getIdentifier(), attributeName, value);
	}
	
	public static void addToListAttribute(Edge edge, String attributeName, String value) {
		addToListAttribute(edgeAtt, edge.getIdentifier(), attributeName, value);
	}
	
	private static void addToListAttribute(CyAttributes attributes, String id, String attributeName, String value) {
		List<String> listAtt = attributes.getListAttribute(id, attributeName);
		if (listAtt == null) {
			listAtt = new ArrayList<String>();
		}
        if (!listAtt.contains(value)) {
            listAtt.add(value);
        }        
		attributes.setListAttribute(id, attributeName, listAtt);
	}
	
	public static void addToListAttributeNonRedundant(Node node, String attributeName, String value) {
		addToListAttributeNonRedundant(nodeAtt, node.getIdentifier(), attributeName, value);
	}
	
	public static void addToListAttributeNonRedundant(Edge edge, String attributeName, String value) {
		addToListAttributeNonRedundant(edgeAtt, edge.getIdentifier(), attributeName, value);
	}
	
	/**Adds the value parameter to the given id's specified list attribute if it isn't already present, creating the attribute if it does not already exist 
	 * @param attributes the attribute structure to be altered
	 * @param id the id of the object for which the attribute should be altered
	 * @param attributeName the name of the list attribute to be altered
	 * @param value the new value to add to the list
	 */
	private static void addToListAttributeNonRedundant(CyAttributes attributes, String id, String attributeName, String value) {
		List<String> listAtt = attributes.getListAttribute(id, attributeName);
		if (listAtt == null) {
			listAtt = new ArrayList<String>();
		}
		if (!listAtt.contains(value)) {
			listAtt.add(value);
			attributes.setListAttribute(id, attributeName, listAtt);
		}
		
	}
	
	public static int getNumThemeMembers(Node themeNode) {
		List<String> themeMembers = nodeAtt.getListAttribute(themeNode.getIdentifier(), TM.memberListAttName);
		if (themeMembers == null) return 0;
		return themeMembers.size();
	}

	public static Node createSumThemeNode(Node node1, Node node2) {
		int mergedId = Cytoscape.getRootGraph().createNode();
		Node merged = Cytoscape.getRootGraph().getNode(mergedId);
		//TODO merge attributes
		return merged;
	}

	public static void setNodeIntAttribute(Node node, String attributeName, int value) {
		nodeAtt.setAttribute(node.getIdentifier(), attributeName, value);
	}

	public static int getNumThemeMembers(Edge themeEdge) {
		List<String> themeMembers = edgeAtt.getListAttribute(themeEdge.getIdentifier(), TM.edgeSourceAttName);
		if (themeMembers == null) return 0;
		return themeMembers.size();
	}

	public static void setEdgeDoubleAttr(Edge edge, String attributeName, double value) {
		edgeAtt.setAttribute(edge.getIdentifier(), attributeName, value);
	}
	
}

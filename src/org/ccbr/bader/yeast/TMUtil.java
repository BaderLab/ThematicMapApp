package org.ccbr.bader.yeast;

import java.util.ArrayList;
import java.util.List;

import giny.model.Node;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

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

	/**Adds the value parameter to the given node's specified list attribute, creating the attribute if it does not already exist 
	 * @param node the node for whom the list attribute should be appended to
	 * @param attributeName the name of the list attribute to be appended
	 * @param value the value to append to the list
	 */
	public static void addToListAttribute(Node node, String attributeName, String value) {
		String identifier = node.getIdentifier();
		List<String> listAtt = nodeAtt.getListAttribute(identifier, attributeName);
		if (listAtt == null) {
			listAtt = new ArrayList<String>();
		}
		listAtt.add(value);
		nodeAtt.setListAttribute(identifier, attributeName, listAtt);
	}

	public static List<String> getStringListAtt(Node node, String attName) {
		//TODO consider placing a check to make sure it is a string list
		return nodeAtt.getListAttribute(node.getIdentifier(), attName);
	}

	public static void setStringAtt(Node node, String attributeName, String value) {
		nodeAtt.setAttribute(node.getIdentifier(), attributeName, value);
	}
	
}

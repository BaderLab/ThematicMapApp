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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;


/**
 * Provides a number of utility methods useful by other parts of the plugin
 */
public class TMUtil {
	
	
	/**
	 * Return the String attribute value from the node table.
	 */
	public static String getStringAtt(CyNetwork network, CyNode node, String attName) {
		return network.getRow(node).get(attName, String.class);
	}
	
	/**
	 * Handles attributes that might be single or list in the same way.
	 */
	public static Collection<?> getAttValues(CyNetwork network, CyNode node, String attName) {
		CyColumn column = network.getDefaultNodeTable().getColumn(attName);
        if(List.class.equals(column.getType())) {
        	List<?> vals = network.getRow(node).getList(attName, column.getListElementType());
        	return vals == null ? Collections.<Object>emptyList() : vals;
        }
        else {
        	Object val = network.getRow(node).get(attName, column.getType());
        	return val == null ? Collections.<Object>emptyList() : Collections.singleton(val);
        }
    }
	
	public static <T> void addToListAttribute(CyRow row, String attributeName, T value, Class<T> type) {
		List<T> listAtt = row.getList(attributeName, type);
		if(listAtt == null) {
			listAtt = new ArrayList<T>();
		}
		if(!listAtt.contains(value)) {
			listAtt.add(value);
		}
		row.set(attributeName, listAtt);
	}
	
	public static void addToListAttribute(CyRow row, String attributeName, String value) {
		addToListAttribute(row, attributeName, value, String.class);
	}
	
	public static int getNumThemeMembers(CyNetwork themeNetwork, CyNode themeNode) {
		List<String> themeMembers = themeNetwork.getRow(themeNode).getList(TM.memberListAttName.name, String.class);
		if (themeMembers == null) return 0;
		return themeMembers.size();
	}

	public static int getNumThemeMembers(CyNetwork themeNetwork, CyEdge themeEdge) {
		List<String> themeMembers = themeNetwork.getRow(themeEdge).getList(TM.edgeSourceAttName.name, String.class);
		if (themeMembers == null) return 0;
		return themeMembers.size();
	}
	
	/**
	 * Returns the first edge between the two nodes that matches the given attribute.
	 * This somewhat simulates the Cytoscape.getCyEdge() API from Cytoscape 2.
	 */
	public static CyEdge getCyEdge(CyNetwork network, CyNode source, CyNode target, String attName, String attValue) {
		List<CyEdge> edges = network.getConnectingEdgeList(source, target, CyEdge.Type.ANY);
		for(CyEdge edge : edges) {
			String actualValue = network.getRow(edge).get(attName, String.class);
			if(attValue.equals(actualValue)) {
				return edge;
			}
		}
		return null;
	}
	
}

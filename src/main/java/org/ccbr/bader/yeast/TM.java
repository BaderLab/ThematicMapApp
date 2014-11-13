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
 * * Description: Aggregates global variables and constants for plugin as static fields  
 */
package org.ccbr.bader.yeast;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

/**
 * Defines the columns that are needed for a ThematicMap.
 */
public enum TM {

	edgeSourceMemberCountAttName("ThemeMapper.INPUT_EDGES_COUNT", CyEdge.class, false, Integer.class),
    edgeStatisticAttName("ThemeMapper.EDGE_STATISTIC", CyEdge.class, false, Double.class),
    edgeStatisticTypeAttName("ThemeMapper.EDGE_STATISTIC_TYPE", CyEdge.class, false, String.class),
	memberListAttName("ThemeMapper.THEME_MEMBERS", CyNode.class, true, String.class),
	edgeSourceAttName("ThemeMapper.INPUT_EDGES", CyEdge.class, true, String.class),
	theme_member_count_att_name("ThemeMapper.THEME_MEMBER_COUNT", CyNode.class, false, Integer.class),
    edgeWeightListAttName("ThemeMapper.EDGE_WEIGHT_LIST", CyEdge.class, true, Double.class),
    avgEdgeWeightAttName("ThemeMapper.AVERAGE_EDGE_WEIGHT", CyEdge.class, false, Double.class),
    formattedNameAttributeName("ThemeMapper.FORMATTED_NAME", CyNode.class, false, String.class);
	

	public final Class<? extends CyIdentifiable> tableType;
	public final String name;
	public final boolean isList;
	public final Class<?> type;
	
	
	private TM(String name, Class<? extends CyIdentifiable> tableType, boolean isList, Class<?> type) {
		this.tableType = tableType;
		this.name = name;
		this.isList = isList;
		this.type = type;
	}
	
	
    public static void createColumns(CyNetwork thematicMap) {
    	for(TM tm : values()) {
    		CyTable table = thematicMap.getTable(tm.tableType, CyNetwork.LOCAL_ATTRS);  // select node or edge table
    		if(tm.isList)
    			table.createListColumn(tm.name, tm.type, false);
    		else
    			table.createColumn(tm.name, tm.type, false);
    	}
    }
    
}

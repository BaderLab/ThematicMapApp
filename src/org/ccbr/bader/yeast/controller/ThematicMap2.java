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
 * * Description: Prototype thematic map session class, currently unused  
 */
package org.ccbr.bader.yeast.controller;

import giny.model.Edge;
import giny.model.Node;

import java.util.List;
import java.util.Map;

import cytoscape.CyNetwork;

public class ThematicMap2 {

	CyNetwork thematicMap;
	Map<Edge,List<Edge>> sThematicMapEdgeToPThematicMapEdge;
	Map<Node,List<Node>> sThematicMapNodeToPThematicMapNode;
	public Map<Edge, List<Edge>> getSThematicMapEdgeToPThematicMapEdge() {
		return sThematicMapEdgeToPThematicMapEdge;
	}
	public void setSThematicMapEdgeToPThematicMapEdge(
			Map<Edge, List<Edge>> thematicMapEdgeToPThematicMapEdge) {
		sThematicMapEdgeToPThematicMapEdge = thematicMapEdgeToPThematicMapEdge;
	}
	public Map<Node, List<Node>> getSThematicMapNodeToPThematicMapNode() {
		return sThematicMapNodeToPThematicMapNode;
	}
	public void setSThematicMapNodeToPThematicMapNode(
			Map<Node, List<Node>> thematicMapNodeToPThematicMapNode) {
		sThematicMapNodeToPThematicMapNode = thematicMapNodeToPThematicMapNode;
	}
	public CyNetwork getThematicMap() {
		return thematicMap;
	}
	public void setThematicMap(CyNetwork thematicMap) {
		this.thematicMap = thematicMap;
	}
	
	public CyNetwork getModifiableThematicMap() {
		return thematicMap;
	}
	
}

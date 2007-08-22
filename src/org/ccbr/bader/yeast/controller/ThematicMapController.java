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
 * * Description: prototype Controller class for plugin, in the MVC pattern sense
 */
package org.ccbr.bader.yeast.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import giny.model.Edge;
import giny.model.Node;
import giny.view.NodeView;

import org.ccbr.bader.yeast.TMUtil;
import org.ccbr.bader.yeast.controller.ThematicMap2;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;

/**Controller class for plugin, in the MVC pattern sense
 * @author mikematan
 *
 */
public class ThematicMapController {

	ThematicMap2 session;
	
	public ThematicMapController(ThematicMap2 thematicMapSession) {
		this.session = thematicMapSession;
	}
	
	public Node mergeThemeNodes(Node node1,Node node2,String mergedName)  {
		//TODO ensure nodes are part of the same network
		//TODO place the merged node in the location half way in between the two original nodes, or perhaps just where the first node was
		if (!session.getThematicMap().containsNode(node1) || !session.getThematicMap().containsNode(node2) ) {
			throw new InvalidMultiGraphOperationException("One or both of the nodes are not theme nodes;  node 1 id '" + node1.getIdentifier() + "', node 2 id '" + node2.getIdentifier() + "'");
		}
		CyNetwork thema = session.getThematicMap();
		
		
		Node merged = TMUtil.createSumThemeNode(node1,node2);  //create a new node with a summation of theme attributes
		thema.addNode(merged);
		
		//place the node in the view at the same position as node 1 was at
		CyNetworkView themaV = Cytoscape.getNetworkView(thema.getIdentifier());
		NodeView node1V = themaV.getNodeView(node1.getRootGraphIndex());
		NodeView node2V = themaV.getNodeView(node2.getRootGraphIndex());
		NodeView mergedV = themaV.getNodeView(merged.getRootGraphIndex());
		mergedV.setXPosition(node1V.getXPosition());
		mergedV.setYPosition(node1V.getYPosition());
		

		
		//remap node and edge mappings between the primary to secondary networks from the old nodes to the new merged node
		remap(node1,node2,merged);
		
		
		

		
//		remove the old versions of the nodes
		//TODO determine if it should be forced removed
		thema.removeNode(node1.getRootGraphIndex(), false);
		thema.removeNode(node2.getRootGraphIndex(), false);
		
		

		
		//return mergeNodes(session.getThematicMap(),node1,node2);
		return null;
	}
	
	private void remap(Node source1, Node source2, Node target) {
		Map<Node,List<Node>> stn2ptn = session.getSThematicMapNodeToPThematicMapNode();
		
		//create the new list of primary theme nodes covered by this secondary/summary theme node
		List<Node> primaryThemeNodesCovered = new ArrayList<Node>();
		primaryThemeNodesCovered.addAll(stn2ptn.get(source1));
		primaryThemeNodesCovered.addAll(stn2ptn.get(source2));
		stn2ptn.put(target,primaryThemeNodesCovered);
		
		//remove the obsolete entries in the primary theme node graph.
		stn2ptn.remove(source1);
		stn2ptn.remove(source2);
		
		//TODO transfer all edged from the original nodes, to the merged node
		Map<Edge,List<Edge>> ste2pte = session.getSThematicMapEdgeToPThematicMapEdge();
		
		CyNetwork thema = session.getModifiableThematicMap();
		int[] edges = thema.getAdjacentEdgeIndicesArray(source1.getRootGraphIndex(), true,true,true);
		for(int e:edges) {
			Edge edge = thema.getEdge(e);
			List<Edge> pedges = ste2pte.get(edge);
		}
	}

	public Node mergeOntologyNodes(Node node1, Node node2) {
		return null;
	}
	
	/**Merges nodes 1 and 2 into a single node, with a summation of attributes, returning the new merged node.  The old nodes are removed.
	 * Also updates the view appropriately.
	 * 
	 * @param containingNetwork network which contains nodes 1 and 2
	 * @param node1 the first of the nodes to be merged
	 * @param node2 the second of the nodes to be merged
	 * @return the new merger node
	 */
	private Node mergeNodes(CyNetwork containingNetwork, Node node1, Node node2) {
		
		return null;
	}
	
}

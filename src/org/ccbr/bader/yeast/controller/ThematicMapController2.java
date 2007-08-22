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

import giny.model.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;

/**Controller class for plugin, in the MVC pattern sense
 * @author mikematan
 *
 */
public class ThematicMapController2 extends ThematicMapController {

	public ThematicMapController2(ThematicMap2 thematicMapSession) {
		super(thematicMapSession);
		this.session = thematicMapSession;
	}


	@Override
	public Node mergeThemeNodes(Node node1, Node node2, String mergedName) {
		super.mergeThemeNodes(node1,node2,mergedName);
		
//		TODO ensure nodes are part of the same network
		//TODO place the merged node in the location half way in between the two original nodes, or perhaps just where the first node was
		if (!session.getThematicMap().containsNode(node1) || !session.getThematicMap().containsNode(node2) ) {
			throw new InvalidMultiGraphOperationException("One or both of the nodes are not theme nodes;  node 1 id '" + node1.getIdentifier() + "', node 2 id '" + node2.getIdentifier() + "'");
		}
		CyNetwork thema = session.getThematicMap();
		CyNetworkView themaV = Cytoscape.getNetworkView(thema.getIdentifier());
		/*
		 * pseudo code
		 * 
		 * determine whether or not either of the nodes to be merged is already a merged node
		 *   if node1 or node2 is a merged node
		 *    use it as the new merged node, and simply merge the other node into it
		 *   else
		 *    create a new merged node, and add the simple nodes to it	
		 */

		if (mergeNodes.contains(node1) && !mergeNodes.contains(node2)) {
			mergeSimpleNodeIntoMergeNode(node2,node1,mergedName);
			//themaV.hideGraphObject(node2);  //hide the node which has been merged into the other one
			hideNode(themaV,node2);
			return node1;
		}
		else if (mergeNodes.contains(node2) && !mergeNodes.contains(node1)) {
			mergeSimpleNodeIntoMergeNode(node1,node2,mergedName);
			themaV.hideGraphObject(node1); //hide the node which has been merged into the other one
			return node1;
		}
		else if (mergeNodes.contains(node2) && mergeNodes.contains(node1)) {
			Node mergeNode =  mergeMergeNodes(node1,node2,mergedName);
			hideNode(themaV,node2);
			hideNode(themaV,node1);
			showNode(themaV,mergeNode);
		}
		else {
			//TODO create new merge node and merge the two simple nodes into it
			Node mergeNode = mergeSimpleNodes(node1,node2,mergedName);
			hideNode(themaV,node2);
			hideNode(themaV,node1);
			showNode(themaV,mergeNode);
		}
		
		return null;
	}
	
	/**
	 * @param view the netw
	 * @param node
	 */
	private void showNode(CyNetworkView view, Node node) {
		view.showGraphObject(node);
	}

	/**
	 * @param view the network view within which the node should be hidden
	 * @param node the node to be hidden within the view
	 */
	private void hideNode(CyNetworkView view, Node node) {
		view.hideGraphObject(node);
	}


	/**
	 * @param mergeNode
	 * @param themeToSplitOff
	 */
	public void sectionThemeNode(Node mergeNode, String themeToSplitOff) {
		
	}
	
	private Node mergeSimpleNodes(Node node1, Node node2, String mergedName) {
		// TODO Auto-generated method stub
		return null;
	}


	private Node mergeMergeNodes(Node mergeNode1, Node mergeNode2, String mergedName) {
		// TODO Auto-generated method stub
		return null;
	}


	private void mergeSimpleNodeIntoMergeNode(Node simpleNode, Node mergeNode, String newMergedName) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This contains the set of merge nodes for this graph.  This is redundant to the keyset of the mergeNodeToContainedNodes set  
	 */
	Set<Node> mergeNodes = new HashSet<Node>();
	
	/**
	 * This maps merge nodes to the simple nodes which it contains.
	 */
	Map<Node,List<Node>> mergeNodeToContainedNodes = new HashMap<Node, List<Node>>();

}

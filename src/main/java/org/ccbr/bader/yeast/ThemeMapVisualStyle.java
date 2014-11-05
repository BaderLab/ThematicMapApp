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
 * * Description: Visual Style class for Thematic Map networks   
 */
package org.ccbr.bader.yeast;

import java.awt.Color;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.vizmap.VisualStyle;


public class ThemeMapVisualStyle extends VisualStyle {

	public ThemeMapVisualStyle(String name) {
		super(name);
		this.setCalculators();
		// TODO Auto-generated constructor stub
	}

	public ThemeMapVisualStyle(VisualStyle toCopy) {
		super(toCopy);
		this.setCalculators();
		// TODO Auto-generated constructor stub
	}

	public ThemeMapVisualStyle(VisualStyle toCopy, String newName) {
		super(toCopy, newName);
		this.setCalculators();
		// TODO Auto-generated constructor stub
	}

	public ThemeMapVisualStyle(String name, NodeAppearanceCalculator nac,
			EdgeAppearanceCalculator eac, GlobalAppearanceCalculator gac) {
		super(name, nac, eac, gac);
		this.setCalculators();
		// TODO Auto-generated constructor stub
	}

	private void setCalculators() {
		this.setNodeAppearanceCalculator(new ThemeMapNodeAppearanceCalculator());
		this.setEdgeAppearanceCalculator(new ThemeMapEdgeAppearanceCalculator());
	}
	
	public class ThemeMapEdgeAppearanceCalculator extends EdgeAppearanceCalculator {

		@Override
		public EdgeAppearance calculateEdgeAppearance(Edge edge, CyNetwork network) {
			// TODO Auto-generated method stub
			EdgeAppearance appr = super.calculateEdgeAppearance(edge, network);
			this.modifyEdgeAppearance(appr, edge, network);
			return appr;
		}

		@Override
		public void calculateEdgeAppearance(EdgeAppearance appr, Edge edge, CyNetwork network) {
			super.calculateEdgeAppearance(appr, edge, network);
			this.modifyEdgeAppearance(appr,edge,network);
		}
		
		private void modifyEdgeAppearance(EdgeAppearance appr, Edge edge, CyNetwork network) {
//			this.modifyEdgeAppearance(appr, edge, network);
			//Color edgeColor = calculateEdgeColorFromHGTestResult(edge);
			//appr.setColor(edgeColor);
			appr.applyBypass(edge);
			return;
		}
		
	}
	
	public class ThemeMapNodeAppearanceCalculator extends NodeAppearanceCalculator {

		CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
		@Override
		public NodeAppearance calculateNodeAppearance(Node node, CyNetwork network) {

			NodeAppearance nodeAppearance = new NodeAppearance();
			modifyNodeAppearance(nodeAppearance, node, network);
			
			return nodeAppearance;
			//return new GoSlimmerNodeAppearance(numDirectlyCoveredGenes,numDirectlyCoveredGenes);
		}
		


		
		private static final int minNodeSize = 2;
		
		Color selectedNodeColor = Color.CYAN;
		Color unselectedNodeColor = new Color(255,150,150);
		
		@Override
		public void calculateNodeAppearance(NodeAppearance appr, Node node, CyNetwork network) {
			
			modifyNodeAppearance(appr, node, network);
			
			appr.applyBypass(node);

		}
		
		private static final int maxNodeLabelLength = 25;
		
//		private boolean isSelectedForSlimSet(Node node) {
//			Boolean isSelected = nodeAtt.getBooleanAttribute(node.getIdentifier(), GOSlimmer.goNodeInSlimSetAttributeName);
//			if (isSelected == null) return false;
//			return isSelected;
//		}
		
		private void modifyNodeAppearance(NodeAppearance appr, Node node, CyNetwork network) {
			
			final int numDirectlyCoveredGenes = TMUtil.getNumThemeMembers(node);
			
			double nodeDim = numDirectlyCoveredGenes>0?numDirectlyCoveredGenes*minNodeSize:minNodeSize;
			nodeDim +=1; //to ensure we don't get any negative values when we calculate the logarithm
			if (nodeDim >1) nodeDim = Math.log(nodeDim);
			nodeDim *= 10;
			
			appr.setLabel(node.getIdentifier());
			appr.setHeight(nodeDim);
			appr.setWidth(nodeDim);
			appr.setShape(ShapeNodeRealizer.ELLIPSE);
			
			
//			if (isSelectedForSlimSet(node)) { 
//				appr.setFillColor(selectedNodeColor);
//			}
//			else {
//				//appr.setFillColor(Color.RED);
//				appr.setFillColor(unselectedNodeColor);
//			}
			
			
//			if (GOSlimmerGUIViewSettings.labelNodesWithOntologyName) {
//				String ontname = nodeAtt.getStringAttribute(node.getIdentifier(), "ontology.name");
//				//only use the first maxNodeLabelLength characters of ontname for the label;  TODO comment out when node tooltips are properly implemented
////				appr.setLabel(ontname.length()<maxNodeLabelLength?ontname:ontname.substring(0, maxNodeLabelLength));
////				appr.setToolTip(ontname);
//				appr.setLabel(ontname);
//			}
//			else {
//				appr.setLabel(node.getIdentifier());
//			}
			appr.setLabel(node.getIdentifier());
			
		}
		
	}

}

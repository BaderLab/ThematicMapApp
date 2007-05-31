package org.ccbr.bader.yeast;

import java.awt.Color;
import java.awt.Shape;
import java.util.List;

import giny.model.Edge;
import giny.model.Node;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.visual.EdgeAppearance;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.LineType;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.ShapeNodeRealizer;
import cytoscape.visual.VisualStyle;

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
			// TODO Auto-generated method stub
			super.calculateEdgeAppearance(appr, edge, network);
		}
		
		private void modifyEdgeAppearance(EdgeAppearance appr, Edge edge, CyNetwork network) {
			this.modifyEdgeAppearance(appr, edge, network);
			//appr.setLineType(LineType.LINE_3);
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

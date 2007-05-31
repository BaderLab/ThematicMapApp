package org.ccbr.bader.yeast;

import giny.model.Edge;
import giny.model.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csplugins.layout.algorithms.hierarchicalLayout.HierarchicalLayoutListener;
import csplugins.layout.algorithms.graphPartition.AttributeCircleLayout;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualMappingManager;

public class ThematicMapFunctionPrototype {

	public ThematicMapFunctionPrototype() {
		// TODO Auto-generated constructor stub
	}
	
	/**Creates a new CyNetwork based on the current CyNetwork, using the values of the given themeAttributeName parameter for the nodes 
	 * of the graph as the themes (to which the nodes belong in the theme graph)
	 * 
	 * @param themeAttributeName the attribute on which the thematic map is to be created
	 */
	public static void createThematicMap(String themeAttributeName) {
		CyNetwork inputNetwork = Cytoscape.getCurrentNetwork();
		CyNetwork thematicMap = Cytoscape.createNetwork(inputNetwork.getTitle() + "_thematic_map");
		/*  pseudo code:
		 *  create a new  attribute on the thematic map group with the name 'node count'
		 *  (alternatively, create a new list attribute with the name 'input nodes')
		 *  create a map linking theme attribute values to the nodes which correspond to them, themeToNode
		 *  iterate through the input network's nodes
		 *   grab the value of the theme attribute corresponding to the node
		 *   if this is a newly encountered value, 
		 *    create a new theme node for this attribute, 
		 *    add a map from the theme name to the theme node to themeToNode 
		 *   increment the node count for that attribute to 1
		 *   (alternatively, add the input node id to the theme node's list attribute 'input nodes' )
		 *   We may also want to map input nodes directly to theme nodes, but I'm not sure this is fully necessary
		 *  
		 *  Iterate through edges in the network
		 *   for each edge connecting two input nodes which are in distinct theme nodes, create an edge linking the two 
		 *   theme nodes
		 *   (alternatively, increment the 'weight' attribute of the theme edge)
		 *  
		 */
		
		CyNetwork in = inputNetwork;
		CyNetwork thema = thematicMap;
		
		//this will map the names of themes to the theme nodes which represent them in the graph
		Map<String,Node> themeNameToThemeNode = new HashMap<String,Node>();
		
		//this maps the ids of input nodes to lists of the themes to which they belong;  this is probably something which we'll want to persist 
		//for the length of the session, since it will probably be useful in later manipulations
		Map<String,Set<String>> inputNodeIDToThemes = new HashMap<String, Set<String>>();
		
		Iterator<Node> nodeI = inputNetwork.nodesIterator();
		//iterate through the input nodes and determine which theme node to map them to
		while(nodeI.hasNext()) {
			Node inode = nodeI.next();
			//TODO we'll need to identify the type of the attribute and adjust our behaviour accordingly; for now we assume it is a string attribute
			byte type = TMUtil.getNodeAttType(themeAttributeName);
			if (type == CyAttributes.TYPE_STRING) {
				String themeAttVal = TMUtil.getStringAtt(inode, themeAttributeName);
				
				//add the input node to the set of member nodes belonging to the theme, creating a new theme node if one does not already exist
				assignNodeToTheme(inode,themeAttVal,themeNameToThemeNode,thema);
				
				//map the input node to this theme 
				addThemeToNodeThemeSet(inputNodeIDToThemes, inode, themeAttVal);
			}
			else if (type == CyAttributes.TYPE_SIMPLE_LIST) {
				//may need to map node to multiple themes
				//iterate through the list of theme values and process them in turn, as above with the single theme case
				List<String> themeAttVals = TMUtil.getStringListAtt(inode, themeAttributeName);
				for(String theme:themeAttVals) {
					//add the input node to the set of member nodes belonging to the theme, creating a new theme node if one does not already exist
					assignNodeToTheme(inode,theme,themeNameToThemeNode,thema);
					
					//map the input node to this theme 
					addThemeToNodeThemeSet(inputNodeIDToThemes, inode, theme);
				}
			}
			else {
				throw new RuntimeException("Unsupported theme attribute type '" + Integer.valueOf(type) + "'.  Only String type '" + CyAttributes.TYPE_STRING + "' supported.");
			}
		}
		
		//iterate through the input node's edges, and determine which theme node to map them to
		Iterator<Edge> edgeI = in.edgesIterator();
		while(edgeI.hasNext()) {
			Edge iedge = edgeI.next();
			//determine whether this edge links inodes which belong to distinct theme nodes

			connectThemeNodesWithConnectedInputNodes(thema,inputNodeIDToThemes,iedge);
		}
		
		//finally, generate the graph view
		CyNetworkView themaView = Cytoscape.createNetworkView(thema);
		
		VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
        if (!vmm.getCalculatorCatalog().getVisualStyleNames().contains("ThemeMapVS")) {
            vmm.getCalculatorCatalog().addVisualStyle(new ThemeMapVisualStyle(vmm.getVisualStyle(),"ThemeMapVS"));
        }
		vmm.setVisualStyle("ThemeMapVS");
		themaView.redrawGraph(false, false);
		//HierarchicalLayoutListener hll = new HierarchicalLayoutListener();
		AttributeCircleLayout all = new AttributeCircleLayout(thema,Cytoscape.getNodeAttributes(),themeAttributeName);
		all.layout();
		themaView.redrawGraph(true, false);
		//hll will execute the layout algorithm on the currently selected view, which should be our newly created one
		//hll.actionPerformed(null);
		
		
		//TODO consider creating 'relationship edges', ie edges between theme nodes which have member input nodes in common;  see: http://www.cgl.ucsf.edu/Research/cytoscape/groupAPI/doc/edu/ucsf/groups/GroupManager.html#regroupGroup(cytoscape.CyNode,%20boolean,%20boolean)
		
	}
	
	/**Assigns the input graph node to the given theme node in the theme network, creating a new theme node in the theme network if one does 
	 * not already exist
	 * 
	 * @param inputNode the input graph node which is being assigned as belonging to a particular theme in the theme graph
	 * @param themeName the name of the theme to which the input node belongs
	 * @param themeNameToThemeNode the map from theme names to corresponding theme nodes in the thematic network
	 * @param themeNetwork the thematic network
	 */
	private static void assignNodeToTheme(Node inputNode, String themeName, Map<String, Node> themeNameToThemeNode, CyNetwork themeNetwork) {
		if (!themeNameToThemeNode.containsKey(themeName)) {
			//no theme node exists for this theme type, so create it first and add it to the map
			//thema.addNode(new Node(null, type));
			int themeNodeRootI = Cytoscape.getRootGraph().createNode();
			int themeNodeI = themeNetwork.addNode(themeNodeRootI);
			Node themeNode = themeNetwork.getNode(themeNodeI);
			themeNode.setIdentifier(themeName);
			TMUtil.setStringAtt(themeNode,themeNode.getIdentifier(),themeName);
			
			themeNameToThemeNode.put(themeName, themeNode);
		}
		
		//retrieve the theme node corresponding to this theme
		Node themeNode = themeNameToThemeNode.get(themeName);
		
		//add the input node to the theme node's member node list
		TMUtil.addToListAttribute(themeNode,TM.memberListAttName,inputNode.getIdentifier());
		// TODO Auto-generated method stub
		
	}

	/**Creates edges between distinct theme nodes which have member input nodes connected by the specified edge 
	 * 
	 * @param thema the thematic map network which contains the input nodes
	 * @param inputNodeIDToThemes
	 * @param edge
	 */
	private static void connectThemeNodesWithConnectedInputNodes(CyNetwork thematicGraph, Map<String, Set<String>> inputNodeIDToThemes, Edge inputEdge) {
		CyNetwork thema = thematicGraph;
		Node source = inputEdge.getSource();
		Node target = inputEdge.getTarget();
		Set<String> sourceNodeThemes = inputNodeIDToThemes.get(source.getIdentifier());
		Set<String> targetNodeThemes = inputNodeIDToThemes.get(target.getIdentifier());
		
		//if there are no source nor target themes for the nodes, their won't be any connections in the theme graph, so ignore
		if (sourceNodeThemes == null || targetNodeThemes == null) return;
		
//		for(String sourceNodeTheme: sourceNodeThemes) {
//			for(String targetNodeTheme:targetNodeThemes) {
//				if (sourceNodeTheme != targetNodeTheme) {
//					//create edge between theme nodes
//					Node sourceThemeNode = Cytoscape.getCyNode(sourceNodeTheme);
//					Node targetThemeNode = Cytoscape.getCyNode(targetNodeTheme);
//					//name the edge after the input edge's source and target nodes
//					String themeEdgeAttributeVal = source.getIdentifier() + "-" + target.getIdentifier();
//					Edge themeEdge = Cytoscape.getCyEdge(sourceThemeNode,targetThemeNode,Semantics.INTERACTION,themeEdgeAttributeVal,true,true);
//					//consider adding this edge to a map, possibly mapping input edge nodes to their list of corresponding theme edges
//					thema.addEdge(themeEdge);
//				}
//			}
//		}
		
		final String themeEdgeAliasVal = "tt";  //for theme-theme
		//maps input edges to the theme edges which they imply
		Map<Edge,Set<Edge>> inputEdgeToThemeEdges = new HashMap<Edge, Set<Edge>>();
		for(String sourceNodeTheme:sourceNodeThemes) {
			for(String targetNodeTheme:targetNodeThemes) {
				if (sourceNodeTheme != targetNodeTheme) {
					//create edge between theme nodes if it doesn't already exist
					Node sourceThemeNode = Cytoscape.getCyNode(sourceNodeTheme);
					Node targetThemeNode = Cytoscape.getCyNode(targetNodeTheme);
					Edge themeEdge = Cytoscape.getCyEdge(sourceThemeNode,targetThemeNode,Semantics.INTERACTION,themeEdgeAliasVal,true,true);
					
					//recording the input edge's source and target nodes as an attribute of the theme edge
					String themeEdgeAttributeVal = source.getIdentifier() + "-" + target.getIdentifier();
					TMUtil.addToListAttribute(themeEdge, TM.edgeSourceAttName, themeEdgeAttributeVal);
					
					thema.addEdge(themeEdge);
					
					//add new theme edge entry to the set of theme edges which this input edge corresponds to
					Set<Edge> themeEdges = inputEdgeToThemeEdges.get(inputEdge);
					if (themeEdges == null) { 
						themeEdges = new HashSet<Edge>();
						inputEdgeToThemeEdges.put(inputEdge, themeEdges);
					}
					themeEdges.add(themeEdge);
				}
			}
		}
	}

	/**Modifies the passed map so that the Set which the Map maps the node's ID to contains the theme parameter.
	 * 
	 * IE, the 'theme' String is added to the Set which the node's ID is mapped to by the Map parameter
	 * 
	 * @param inputNodeIDToThemes the map from input node IDs to sets of themes which the input node belongs to
	 * @param node the input node who's associated theme set is to be added to
	 * @param theme the name of the theme to be added to the node's themes
	 */
	private static void addThemeToNodeThemeSet(Map<String,Set<String>> inputNodeIDToThemes, Node node, String theme) {
		String id = node.getIdentifier();
		Set<String> themeSet = inputNodeIDToThemes.get(id);
		if (themeSet == null) {
			themeSet = new HashSet<String>();
		}
		themeSet.add(theme);
		inputNodeIDToThemes.put(id, themeSet);
	}

}

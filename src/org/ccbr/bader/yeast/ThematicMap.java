package org.ccbr.bader.yeast;

import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.Cytoscape;
import cytoscape.CyNetwork;
import cytoscape.layout.LayoutTask;
import cytoscape.task.util.TaskManager;
import cytoscape.visual.*;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.mappings.*;
import cytoscape.view.CyNetworkView;

import java.awt.*;
import java.awt.List;
import java.util.*;

import giny.model.Node;
import giny.model.Edge;
import csplugins.layout.algorithms.graphPartition.AttributeCircleLayout;

public class ThematicMap {

    private final CyAttributes edgeAtt = Cytoscape.getEdgeAttributes();
    private final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
    private final String themeMapVisualStyle = "ThemeMapVS";
    private java.util.List<Color> colorPalette = null;
    private String edgeWeightAttributeName = "";

    //this will map the names of themes to the theme nodes which represent them in the graph
    private Map<Object, Node> themeNameToThemeNode = new HashMap<Object,Node>();

    // Options for edge width calculations
    public static final int EDGE_WIDTH_COUNT = 0;
    public static final int EDGE_WIDTH_STATISTICS = 1;

    public ThematicMap() {
		// TODO Auto-generated constructor stub

    }

    public void setEdgeWeightAttributeName(String edgeWeightAttName) {
        edgeWeightAttributeName = edgeWeightAttName;
    }

    public CyNetwork createThematicMap(String themeAttributeName) {
		CyNetwork inputNetwork = Cytoscape.getCurrentNetwork();
		return createThematicMap(inputNetwork, themeAttributeName);
	}

	/**Creates a new CyNetwork based on the current CyNetwork, using the values of the given themeAttributeName parameter for the nodes
	 * of the graph as the themes (to which the nodes belong in the theme graph)
	 *
	 * @param themeAttributeName the attribute on which the thematic map is to be created
	 */
	public CyNetwork createThematicMap(CyNetwork inputNetwork,String themeAttributeName) {

		CyNetwork thematicMap = Cytoscape.createNetwork(inputNetwork.getTitle() + " - " + themeAttributeName + " thematic map");
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

		//this maps the ids of input nodes to lists of the themes to which they belong;  this is probably something which we'll want to persist
		//for the length of the session, since it will probably be useful in later manipulations
		Map<String,Set<Object>> inputNodeIDToThemes = new HashMap<String, Set<Object>>();

        byte type = TMUtil.getNodeAttType(themeAttributeName);
		Iterator<Node> nodeI = inputNetwork.nodesIterator();
		//iterate through the input nodes and determine which theme node to map them to
		while(nodeI.hasNext()) {
			Node inode = nodeI.next();
			//TODO we'll need to identify the type of the attribute and adjust our behaviour accordingly; for now we assume it is a string attribute
            Set<Object> themeAttVals = getAttValues(inode, themeAttributeName, type);
            for (Object themeAttVal:themeAttVals) {
                //add the input node to the set of member nodes belonging to the theme, creating a new theme node if one does not already exist
				assignNodeToTheme(inode,themeAttVal,themeNameToThemeNode,thema);

				//map the input node to this theme
				addThemeToNodeThemeSet(inputNodeIDToThemes, inode, themeAttVal);

            }

		}

		//iterate through the input node's edges, and determine which theme node to map them to
		Iterator<Edge> edgeI = in.edgesIterator();
		while(edgeI.hasNext()) {
			Edge iedge = edgeI.next();
			//determine whether this edge links inodes which belong to distinct theme nodes

			connectThemeNodesWithConnectedInputNodes(thema,inputNodeIDToThemes,iedge);
		}

		//iterate through the theme nodes, creating the THEME_MEMBER_COUNT attribute
		nodeI = thema.nodesIterator();
		while(nodeI.hasNext()) {
			Node themeNode = nodeI.next();
			int themeMemberCount = TMUtil.getNumThemeMembers(themeNode);
			TMUtil.setNodeIntAttribute(themeNode,TM.theme_member_count_att_name,themeMemberCount);
		}

        // iterate through the theme edges, creating the INPUT_EDGES_COUNT attribute, the AVERAGE_EDGE_WEIGHT attribute,
        // the EDGE_STATISTIC attribute and the EDGE_STATISTIC_TYPE attribute
		Iterator themeEdgesI = thema.edgesIterator();
        while(themeEdgesI.hasNext()) {
            Edge mapEdge = (Edge) themeEdgesI.next();
            int themeMemberCount = TMUtil.getNumThemeMembers(mapEdge);
            edgeAtt.setAttribute(mapEdge.getIdentifier(), TM.edgeSourceMemberCountAttName, themeMemberCount);

            // create EDGE_STATISTIC attribute and initialize to INPUT_EDGES_COUNT value
            edgeAtt.setAttribute(mapEdge.getIdentifier(), TM.edgeStatisticAttName, (double)themeMemberCount);

            // create EDGE_STATISTIC_TYPE attribute and initialize to 'COUNT'
            edgeAtt.setAttribute(mapEdge.getIdentifier(),TM.edgeStatisticTypeAttName,"COUNT");

            // if weight attribute was initialized, create AVERAGE_EDGE_WEIGHT attribute
            if (!edgeWeightAttributeName.equals("")) {
                java.util.List<Double> weightList = edgeAtt.getListAttribute(mapEdge.getIdentifier(), TM.edgeWeightListAttName);

                double avgWeight = 0.0;
                if (weightList != null && weightList.size() > 0) {
                    for (Double weight : weightList) {
                        avgWeight = avgWeight + weight;
                    }
                    avgWeight = avgWeight / (weightList.size() * 1.0);
                    edgeAtt.setAttribute(mapEdge.getIdentifier(), TM.avgEdgeWeightAttName, avgWeight);
                }
            }

        }

        //TODO consider creating 'relationship edges', ie edges between theme nodes which have member input nodes in common;  see: http://www.cgl.ucsf.edu/Research/cytoscape/groupAPI/doc/edu/ucsf/groups/GroupManager.html#regroupGroup(cytoscape.CyNode,%20boolean,%20boolean)
		return thema;
	}

    public void getSingleNodes(CyNetwork inputNetwork, CyNetwork thematicMap, String attName, Set<Node> single_nodes, Set<Edge> single_node_edges) {
        byte type = TMUtil.getNodeAttType(attName);
        Set<Node> unassigned_nodes = new HashSet<Node>();
        Set<Edge> unassigned_edges = new HashSet<Edge>();

        // find unassigned nodes from the original network
        Iterator orig_nodes_i = inputNetwork.nodesIterator();
        while (orig_nodes_i.hasNext()) {
            Node orig_node = (Node) orig_nodes_i.next();

            Set<Object> themeAttVals = getAttValues(orig_node, attName, type);
            if (themeAttVals.isEmpty()) {
                //System.out.println("unassigned node: " + orig_node.toString());
                unassigned_nodes.add(orig_node);
            }

        }

        // create nodes in thematic map for each of the unassigned nodes in the original network
        for (Node unassignedNode: unassigned_nodes) {
            int nodeIndex = unassignedNode.getRootGraphIndex();
            thematicMap.addNode(nodeIndex);
            nodeAtt.setAttribute(unassignedNode.getIdentifier(), TM.theme_member_count_att_name, 1);

            //add the input node to the theme node's member node list
		    TMUtil.addToListAttribute(unassignedNode,TM.memberListAttName,unassignedNode.getIdentifier());

        }


        // find edges connecting unassigned nodes
        Iterator orig_edges_i = inputNetwork.edgesIterator();
        while (orig_edges_i.hasNext()) {
            Edge orig_edge = (Edge) orig_edges_i.next();

            String interact = edgeAtt.getStringAttribute(orig_edge.getIdentifier(), Semantics.INTERACTION);
            Node source = orig_edge.getSource();
            Node target = orig_edge.getTarget();

            // NOT SURE ABOUT THIS OPTION
            if (unassigned_nodes.contains(source) && unassigned_nodes.contains(target)) {

                Edge themeNodeEdge = Cytoscape.getCyEdge(source, target, Semantics.INTERACTION, interact + "_tt", false, false);
                // if the edge does not exist, create it
                if (themeNodeEdge == null) {
                    themeNodeEdge = Cytoscape.getCyEdge(source, target, Semantics.INTERACTION, interact + "_tt", true, false);
                    edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeStatisticAttName, 0.0);
                    edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeSourceMemberCountAttName, 1);
                    edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeStatisticTypeAttName, "COUNT");
                    //recording the input edge's source and target nodes as an attribute of the theme edge

                } else {
                    // get current count
                    int count = edgeAtt.getIntegerAttribute(themeNodeEdge.getIdentifier(), TM.edgeSourceMemberCountAttName);
                    edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeSourceMemberCountAttName, count + 1);
                }
                String themeEdgeAttributeVal = source.getIdentifier() + "-" + target.getIdentifier();
                TMUtil.addToListAttribute(themeNodeEdge, TM.edgeSourceAttName, themeEdgeAttributeVal);
                thematicMap.addEdge(themeNodeEdge);

                //unassigned_edges.add(orig_edge);
            }
            else if (unassigned_nodes.contains(source)) {
                Set<Object> themeAttVals = getAttValues(target, attName, type);
                for (Object themeAttVal : themeAttVals) {
                    Node themeAttNode = themeNameToThemeNode.get(themeAttVal);

                    Edge themeNodeEdge = Cytoscape.getCyEdge(source, themeAttNode, Semantics.INTERACTION, interact + "_tt", false, false);
                    // if the edge does not exist, create it
                    if (themeNodeEdge == null) {
                        themeNodeEdge = Cytoscape.getCyEdge(source, themeAttNode, Semantics.INTERACTION, interact + "_tt", true, false);
                        edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeStatisticAttName, 0.0);
                        edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeSourceMemberCountAttName, 1);
                        edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeStatisticTypeAttName, "COUNT");
                        //recording the input edge's source and target nodes as an attribute of the theme edge

                    }
                    else {
                        // get current count
                        int count = edgeAtt.getIntegerAttribute(themeNodeEdge.getIdentifier(), TM.edgeSourceMemberCountAttName);
                        edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeSourceMemberCountAttName, count+1);
                    }
                    String themeEdgeAttributeVal = source.getIdentifier() + "-" + target.getIdentifier();
                    TMUtil.addToListAttribute(themeNodeEdge, TM.edgeSourceAttName, themeEdgeAttributeVal);
                    thematicMap.addEdge(themeNodeEdge);
                }


            }
            else if (unassigned_nodes.contains(target)) {
                Set<Object> themeAttVals = getAttValues(source, attName, type);
                 for (Object themeAttVal:themeAttVals) {
                     Node themeAttNode = themeNameToThemeNode.get(themeAttVal);

                     Edge themeNodeEdge = Cytoscape.getCyEdge(themeAttNode, target, Semantics.INTERACTION, interact+"_tt", false, false);
                     // if the edge does not exist, create it
                     if (themeNodeEdge == null) {
                         themeNodeEdge = Cytoscape.getCyEdge(themeAttNode, target, Semantics.INTERACTION, interact+"_tt", true, false);
                         edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeStatisticAttName, 0.0);
                         edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeSourceMemberCountAttName, 1);
                         edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeStatisticTypeAttName, "COUNT");
                     }
                     else {
                         // get current count
                         int count = edgeAtt.getIntegerAttribute(themeNodeEdge.getIdentifier(), TM.edgeSourceMemberCountAttName);
                         edgeAtt.setAttribute(themeNodeEdge.getIdentifier(), TM.edgeSourceMemberCountAttName, count+1);
                     }
                     String themeEdgeAttributeVal = source.getIdentifier() + "-" + target.getIdentifier();
                     TMUtil.addToListAttribute(themeNodeEdge, TM.edgeSourceAttName, themeEdgeAttributeVal);
                     thematicMap.addEdge(themeNodeEdge);
                 }
            }

        }
    }

    /**Assigns the input graph node to the given theme node in the theme network, creating a new theme node in the theme network if one does
	 * not already exist
	 *
	 * @param inputNode the input graph node which is being assigned as belonging to a particular theme in the theme graph
	 * @param themeName the theme to which the input node belongs
	 * @param themeNameToThemeNode the map from theme names to corresponding theme nodes in the thematic network
	 * @param themeNetwork the thematic network
	 */
    private void assignNodeToTheme(Node inputNode, Object themeName, Map<Object, Node> themeNameToThemeNode, CyNetwork themeNetwork) {
        if (themeName==null) {
            return;
        }
        if (!themeNameToThemeNode.containsKey(themeName)) {
			//no theme node exists for this theme type, so create it first and add it to the map
			//thema.addNode(new Node(null, type));
			int themeNodeRootI = Cytoscape.getRootGraph().createNode();
			int themeNodeI = themeNetwork.addNode(themeNodeRootI);
			Node themeNode = themeNetwork.getNode(themeNodeI);
            themeNode.setIdentifier(themeName.toString());
			//TMUtil.setStringAtt(themeNode,themeNode.getIdentifier(),themeName);

			themeNameToThemeNode.put(themeName, themeNode);
		}

		//retrieve the theme node corresponding to this theme
		Node themeNode = themeNameToThemeNode.get(themeName);

		//add the input node to the theme node's member node list
		TMUtil.addToListAttribute(themeNode,TM.memberListAttName,inputNode.getIdentifier());
		// TODO Auto-generated method stub

	}

    private void connectThemeNodesWithConnectedInputNodes(CyNetwork thematicGraph, Map<String, Set<Object>> inputNodeIDToThemes, Edge inputEdge) {
		String interact = edgeAtt.getStringAttribute(inputEdge.getIdentifier(), Semantics.INTERACTION);
        CyNetwork thema = thematicGraph;
		Node source = inputEdge.getSource();
		Node target = inputEdge.getTarget();
		Set<Object> sourceNodeThemes = inputNodeIDToThemes.get(source.getIdentifier());
		Set<Object> targetNodeThemes = inputNodeIDToThemes.get(target.getIdentifier());

		//if there are no source nor target themes for the nodes, their won't be any connections in the theme graph, so ignore
		if (sourceNodeThemes == null || targetNodeThemes == null || sourceNodeThemes.size() ==0 || targetNodeThemes.size() == 0) {
            return;
        }

        // check and see if edge weight attribute name has been set
        // if so, add the weight for this edge to the list of weights for the theme edge
        double inputEdgeWeight = Double.MIN_VALUE;
        if (!edgeWeightAttributeName.equals("")) {
            Byte type = edgeAtt.getType(edgeWeightAttributeName);

            if (type.equals(CyAttributes.TYPE_FLOATING)) {
                inputEdgeWeight = edgeAtt.getDoubleAttribute(inputEdge.getIdentifier(), edgeWeightAttributeName);

            }
            else if (type.equals(CyAttributes.TYPE_INTEGER)) {
                inputEdgeWeight = (double) edgeAtt.getIntegerAttribute(inputEdge.getIdentifier(), edgeWeightAttributeName);
            }
        }


    	final String themeEdgeAliasVal = interact + "_tt";  //for theme-theme
		//maps input edges to the theme edges which they imply
		Map<Edge,Set<Edge>> inputEdgeToThemeEdges = new HashMap<Edge, Set<Edge>>();
		for(Object sourceNodeTheme:sourceNodeThemes) {
			for(Object targetNodeTheme:targetNodeThemes) {
                //create edge between theme nodes if it doesn't already exist
                Node sourceThemeNode = Cytoscape.getCyNode(sourceNodeTheme.toString());
                Node targetThemeNode = Cytoscape.getCyNode(targetNodeTheme.toString());

                //TODO if we add a mode for capturing directedness, change directedness parameter in the following statements to 'true'
                Edge themeEdge = Cytoscape.getCyEdge(sourceThemeNode, targetThemeNode, Semantics.INTERACTION, themeEdgeAliasVal, false, false);
                //if the edge does not exist, create it.
                if (themeEdge == null) {
                    themeEdge = Cytoscape.getCyEdge(sourceThemeNode, targetThemeNode, Semantics.INTERACTION, themeEdgeAliasVal, true, false);
                    TMUtil.setEdgeDoubleAttr(themeEdge, TM.edgeStatisticAttName, 0.0);
                }

                //recording the input edge's source and target nodes as an attribute of the theme edge
                String themeEdgeAttributeVal = source.getIdentifier() + "-" + target.getIdentifier();
                TMUtil.addToListAttribute(themeEdge, TM.edgeSourceAttName, themeEdgeAttributeVal);

                // add the input edge weight to the list of weights (if applicable)
                if (inputEdgeWeight!=Double.MIN_VALUE) {
                    java.util.List<Double> weightList = edgeAtt.getListAttribute(themeEdge.getIdentifier(), TM.edgeWeightListAttName);
                    if (weightList == null) {
                        weightList = new ArrayList<Double>();
                    }
                    weightList.add(inputEdgeWeight);
                    edgeAtt.setListAttribute(themeEdge.getIdentifier(), TM.edgeWeightListAttName, weightList);
                }

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

    /**Modifies the passed map so that the Set which the Map maps the node's ID to contains the theme parameter.
	 *
	 * IE, the 'theme' object is added to the Set which the node's ID is mapped to by the Map parameter
	 *
	 * @param inputNodeIDToThemes the map from input node IDs to sets of themes which the input node belongs to
	 * @param node the input node who's associated theme set is to be added to
	 * @param theme the theme to be added to the node's themes
	 */
	private void addThemeToNodeThemeSet(Map<String,Set<Object>> inputNodeIDToThemes, Node node, Object theme) {
		String id = node.getIdentifier();
		Set<Object> themeSet = inputNodeIDToThemes.get(id);
		if (themeSet == null) {
			themeSet = new HashSet<Object>();
		}
		if (theme != null) {
            themeSet.add(theme);
        }
		inputNodeIDToThemes.put(id, themeSet);
	}

	public CyNetworkView createThematicMapDefaultView(CyNetwork thema,String themeAttributeName, int edgeWidthType) {
		//finally, generate the graph view
		CyNetworkView themaView = Cytoscape.createNetworkView(thema);


		VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
		if (!vmm.getCalculatorCatalog().getVisualStyleNames().contains(themeMapVisualStyle)) {
            //vmm.getCalculatorCatalog().addVisualStyle(new ThemeMapVisualStyle(vmm.getVisualStyle(),themeMapVisualStyle));
            VisualStyle vs = createThemeMapVisualStyle(vmm.getVisualStyle(), thema, edgeWidthType);
            vmm.getCalculatorCatalog().addVisualStyle(vs);
        }
		vmm.setVisualStyle(themeMapVisualStyle);
		themaView.redrawGraph(false, false);
		//HierarchicalLayoutListener hll = new HierarchicalLayoutListener();
        AttributeCircleLayout all = new AttributeCircleLayout(true);
        all.setLayoutAttribute(themeAttributeName);

        TaskManager.executeTask( new LayoutTask(all, Cytoscape.getCurrentNetworkView()),
			                         LayoutTask.getDefaultTaskConfig() );


        //AttributeCircleLayout all = new AttributeCircleLayout(thema,Cytoscape.getNodeAttributes(),themeAttributeName);
		//all.layout();
		themaView.redrawGraph(true, false);
		//hll will execute the layout algorithm on the currently selected view, which should be our newly created one
		//hll.actionPerformed(null);

		// prototype code for using a mapping to determine the size of the nodes, as opposed to hardcoding the nodesizecalculator, which is how it is currently done
//		double defaultSize = 10.0;
//		ContinuousMapping sizeMapping = new ContinuousMapping(defaultSize,ObjectMapping.NODE_MAPPING);
//		sizeMapping.setControllingAttributeName(TM.theme_member_count_att_name, thema, true);
//		VisualStyle themaVS = new ThemeMapVisualStyle(vmm.getVisualStyle(),"ThemeMapVS");
		//themaVS.getNodeAppearanceCalculator().getNodeShapeCalculator().

		return themaView;
	}


	private VisualStyle createThemeMapVisualStyle(VisualStyle vs, CyNetwork network, int edgeWidthType) {

        // Loop through the nodes in the network to find the max and min member counts
        int maxMemberCount = Integer.MIN_VALUE;
        int minMemberCount = Integer.MAX_VALUE;

        Iterator nodes_i = network.nodesIterator();
        while (nodes_i.hasNext()) {
            Node node = (Node) nodes_i.next();
            int count = nodeAtt.getIntegerAttribute(node.getIdentifier(), TM.theme_member_count_att_name);
            if (count>maxMemberCount) {
                maxMemberCount = count;
            }
            if (count<minMemberCount) {
                minMemberCount = count;
            }
        }

        // Loop through the edges in the network to find a unique list of types, as well as the
        // max and min for edge width calculations
        Set<String> edgeTypes = new HashSet<String>();
        double maxEdgeWidthAtt = Double.MIN_VALUE;
        double minEdgeWidthAtt = Double.MAX_VALUE;

        Iterator edges_i = network.edgesIterator();
        while (edges_i.hasNext()) {
            Edge edge = (Edge) edges_i.next();

            double count;
            if (edgeWidthType == EDGE_WIDTH_STATISTICS) {
                count = edgeAtt.getDoubleAttribute(edge.getIdentifier(), TM.edgeStatisticAttName);
            }
            else {
                count = (double) edgeAtt.getIntegerAttribute(edge.getIdentifier(),TM.edgeSourceMemberCountAttName);
            }
            if (count>maxEdgeWidthAtt) {
                maxEdgeWidthAtt = count;
            }
            if (count<minEdgeWidthAtt) {
                minEdgeWidthAtt = count;
            }
            String type = edgeAtt.getStringAttribute(edge.getIdentifier(), "interaction");
            if (!edgeTypes.contains(type)) {
                edgeTypes.add(type);
            }
        }

        NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();
		EdgeAppearanceCalculator edgeAppCalc = new EdgeAppearanceCalculator();
		GlobalAppearanceCalculator globalAppCalc = new GlobalAppearanceCalculator();

        // Set default shape and color
        NodeAppearance na = new NodeAppearance();
        na.set(VisualPropertyType.NODE_SHAPE, NodeShape.ELLIPSE);
        na.set(VisualPropertyType.NODE_FILL_COLOR, new Color(255,150,150));
        nodeAppCalc.setDefaultAppearance(na);

        // Passthrough Mapping - set node label
		PassThroughMapping pm = new PassThroughMapping(new String(), "ID");
		Calculator nlc = new BasicCalculator("Theme Map Node Label Calculator",pm, VisualPropertyType.NODE_LABEL);
		nodeAppCalc.setCalculator(nlc);

        //  Continuous Mapping - set node size (max = 100, min = 10)
        ContinuousMapping cm = new ContinuousMapping(10, ObjectMapping.NODE_MAPPING);
        cm.setControllingAttributeName(TM.theme_member_count_att_name, network, false);

        BoundaryRangeValues node_bv0 = new BoundaryRangeValues(10, 10, 10);
        BoundaryRangeValues node_bv1 = new BoundaryRangeValues(100,100,100);

        cm.addPoint(minMemberCount,node_bv0);
        cm.addPoint(maxMemberCount,node_bv1);

        Calculator nsc = new BasicCalculator("Theme Map Node Size Calculator", cm, VisualPropertyType.NODE_SIZE);
        nodeAppCalc.setCalculator(nsc);

        // Discrete Mapping - set edge color
        DiscreteMapping dm = new DiscreteMapping(Color.BLACK, ObjectMapping.EDGE_MAPPING);
        dm.setControllingAttributeName("interaction",network,false);

        Random gen = new Random();
        Set<Color> allColors = new HashSet<Color>();
        for (String type:edgeTypes) {
            Color col = getColor(gen);
            while (allColors.contains(col)) {
                col = getColor(gen);
            }
            allColors.add(col);
            dm.putMapValue(type, col);
        }

        Calculator ecc = new BasicCalculator("Theme Map Edge Color Calculator", dm, VisualPropertyType.EDGE_COLOR);
		edgeAppCalc.setCalculator(ecc);

        //  Continuous Mapping - set edge width (max = 10, min = 1)

        String edgeWidthAttName = "";
        if (edgeWidthType == EDGE_WIDTH_STATISTICS) {
            edgeWidthAttName = TM.edgeStatisticAttName;
        }
        else {
            edgeWidthAttName = TM.edgeSourceMemberCountAttName;
        }
        ContinuousMapping cem = new ContinuousMapping(1.0, ObjectMapping.EDGE_MAPPING);
        cem.setControllingAttributeName(edgeWidthAttName, network, false);

        BoundaryRangeValues edge_bv0 = new BoundaryRangeValues(1, 1, 1);
        BoundaryRangeValues edge_bv1 = new BoundaryRangeValues(10,10,10);
        cem.addPoint(minEdgeWidthAtt,edge_bv0);
        cem.addPoint(maxEdgeWidthAtt,edge_bv1);
        Calculator esc = new BasicCalculator("Theme Map Edge Width Calculator", cem, VisualPropertyType.EDGE_LINE_WIDTH);
        edgeAppCalc.setCalculator(esc);

        // Set default background color
        globalAppCalc.setDefaultBackgroundColor(new Color(204,204,255));

        // Create the visual style
		VisualStyle visualStyle = new VisualStyle(themeMapVisualStyle, nodeAppCalc, edgeAppCalc, globalAppCalc);


        return visualStyle;
	}

    private void initializeColorPalette() {
        colorPalette = new ArrayList<Color>(20);
        colorPalette.add(Color.BLUE);
        colorPalette.add(Color.RED);       
        colorPalette.add(Color.YELLOW);
        colorPalette.add(Color.ORANGE);
        colorPalette.add(Color.GREEN);
        colorPalette.add(Color.MAGENTA);
        colorPalette.add(Color.CYAN);
        colorPalette.add(Color.PINK);
        colorPalette.add(Color.BLACK);
        colorPalette.add(Color.GRAY);
    }

    private Color getColor(Random gen) {
         // initialize color palette for edge colors
        if (colorPalette ==null) {
            initializeColorPalette();
        }
        Color col = null;
        if (colorPalette.size()!=0) {
            col = colorPalette.remove(0);
        }
        else {
            col = getRandomColor(gen);
        }
        return col;
    }

    private Color getRandomColor(Random gen) {
        int r = gen.nextInt(256);
        int g = gen.nextInt(256);
        int b = gen.nextInt(256);
        return new Color(r,g,b);
    }

    private Set<Object> getAttValues(Node node, String attName, Byte attType) {

        Set<Object> attValues = new HashSet<Object>();

        if (attType == CyAttributes.TYPE_SIMPLE_LIST) {
            java.util.List attValList = nodeAtt.getListAttribute(node.getIdentifier(), attName);
            for (Object attVal: attValList) {
                if (attVal!=null) {
                    attValues.add(attVal);
                }
            }
        }
        else {
            Object attVal = null;
            if (attType == CyAttributes.TYPE_STRING) {
                attVal = nodeAtt.getStringAttribute(node.getIdentifier(), attName);
            }
            else if (attType == CyAttributes.TYPE_BOOLEAN) {
                attVal = nodeAtt.getBooleanAttribute(node.getIdentifier(), attName);
            }
            else if (attType == CyAttributes.TYPE_INTEGER) {
                attVal = nodeAtt.getIntegerAttribute(node.getIdentifier(), attName);
            }
            else if (attType == CyAttributes.TYPE_FLOATING) {
                attVal = nodeAtt.getDoubleAttribute(node.getIdentifier(), attName);
            }
            else {
                throw new RuntimeException ("Unsupported theme attribute type '" + Integer.valueOf(attType) + "'.");
            }

            if (attVal != null) {
                attValues.add(attVal);
            }
        }

        return attValues;
    }

}

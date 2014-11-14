package org.ccbr.bader.yeast;


import java.awt.Color;
import java.awt.Paint;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

import com.google.inject.Inject;
import com.google.inject.name.Named;


public class ThematicMap {

	@Inject private CyApplicationManager applicationManager;
	@Inject private CyNetworkFactory networkFactory;
	@Inject private CyNetworkViewFactory networkViewFactory;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyNetworkManager networkManager;
	@Inject private CyLayoutAlgorithmManager layoutAlgorithmManager;
	@Inject private TaskManager<?,?> taskManager;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	
	@Inject @Named("continuous")  private VisualMappingFunctionFactory functionFactoryContinuous;
	@Inject @Named("discrete")    private VisualMappingFunctionFactory functionFactoryDiscrete;
	@Inject @Named("passthrough") private VisualMappingFunctionFactory functionFactoryPassthrough;
	
	
	
    private static final String THEME_MAP_VISUAL_STYLE = "ThemeMapVS";
    
    private String edgeWeightAttributeName = "";
    private boolean allowSelfEdges = true;

    //this will map the names of themes to the theme nodes which represent them in the graph
    private Map<Object,CyNode> themeNameToThemeNode = new HashMap<Object,CyNode>();

    // Options for edge width calculations
    public static final int EDGE_WIDTH_COUNT = 0;
    public static final int EDGE_WIDTH_STATISTICS = 1;

    
    public ThematicMap() {
    }

    public void setEdgeWeightAttributeName(String edgeWeightAttName) {
        this.edgeWeightAttributeName = edgeWeightAttName;
    }

    public void setAllowSelfEdges(boolean allow) {
    	this.allowSelfEdges = allow;
    }
    
    public CyNetwork createThematicMap(String themeAttributeName) {
		return createThematicMap(applicationManager.getCurrentNetwork(), themeAttributeName);
	}

	/**
	 * Creates a new CyNetwork based on the current CyNetwork, using the values of the given themeAttributeName parameter for the nodes
	 * of the graph as the themes (to which the nodes belong in the theme graph)
	 *
	 * @param themeAttributeName the attribute on which the thematic map is to be created
	 */
	public CyNetwork createThematicMap(CyNetwork inputNetwork, String themeAttributeName) {
		String inputNetworkName = inputNetwork.getRow(inputNetwork).get(CyNetwork.NAME, String.class);
		String name = String.format("%s - %s thematic map", inputNetworkName, themeAttributeName);
		CyNetwork thematicMapNetwork = networkFactory.createNetwork();
		TM.createColumns(thematicMapNetwork);
		thematicMapNetwork.getRow(thematicMapNetwork).set(CyNetwork.NAME, name);
		networkManager.addNetwork(thematicMapNetwork);

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

		//this maps the ids of input nodes to lists of the themes to which they belong;  this is probably something which we'll want to persist
		//for the length of the session, since it will probably be useful in later manipulations
		Map<String,Set<Object>> inputNodeIDToThemes = new HashMap<String, Set<Object>>();

		//iterate through the input nodes and determine which theme node to map them to
		for(CyNode node : inputNetwork.getNodeList()) {
			//TODO we'll need to identify the type of the attribute and adjust our behaviour accordingly; for now we assume it is a string attribute
            Collection<?> themeAttVals = TMUtil.getAttValues(inputNetwork, node, themeAttributeName);
            for(Object themeAttVal : themeAttVals) {
                //add the input node to the set of member nodes belonging to the theme, creating a new theme node if one does not already exist
				assignNodeToTheme(node, themeAttVal, themeNameToThemeNode, thematicMapNetwork);

				//map the input node to this theme
				addThemeToNodeThemeSet(inputNodeIDToThemes, node, themeAttVal);
            }
		}

		//iterate through the input node's edges, and determine which theme node to map them to
		for(CyEdge iedge : inputNetwork.getEdgeList()) {
			//determine whether this edge links inodes which belong to distinct theme nodes
			connectThemeNodesWithConnectedInputNodes(thematicMapNetwork, inputNodeIDToThemes, inputNetwork, iedge);
		}

		//iterate through the theme nodes, creating the THEME_MEMBER_COUNT attribute
		for(CyNode themeNode : thematicMapNetwork.getNodeList()) {
			int themeMemberCount = TMUtil.getNumThemeMembers(thematicMapNetwork, themeNode);
			CyRow row = thematicMapNetwork.getRow(themeNode);
			row.set(TM.theme_member_count_att_name.name, themeMemberCount);
			String formattedName = formatName(row.get(CyNetwork.NAME, String.class));
			row.set(TM.formattedNameAttributeName.name, formattedName);
        }

        // iterate through the theme edges, creating the INPUT_EDGES_COUNT attribute, the AVERAGE_EDGE_WEIGHT attribute,
        // the EDGE_STATISTIC attribute and the EDGE_STATISTIC_TYPE attribute
        for(CyEdge mapEdge : thematicMapNetwork.getEdgeList()) {
            int themeMemberCount = TMUtil.getNumThemeMembers(thematicMapNetwork, mapEdge);
            thematicMapNetwork.getRow(mapEdge).set(TM.edgeSourceMemberCountAttName.name, themeMemberCount);

            // create EDGE_STATISTIC attribute and initialize to INPUT_EDGES_COUNT value
            thematicMapNetwork.getRow(mapEdge).set(TM.edgeStatisticAttName.name, (double)themeMemberCount);

            // create EDGE_STATISTIC_TYPE attribute and initialize to 'COUNT'
            thematicMapNetwork.getRow(mapEdge).set(TM.edgeStatisticTypeAttName.name, "COUNT");

            // if weight attribute was initialized, create AVERAGE_EDGE_WEIGHT attribute
            if (!edgeWeightAttributeName.equals("")) {
                List<Double> weightList = thematicMapNetwork.getRow(mapEdge).getList(TM.edgeWeightListAttName.name, Double.class); 

                double avgWeight = 0.0;
                if (weightList != null && weightList.size() > 0) {
                    for (Double weight : weightList) {
                        avgWeight = avgWeight + weight;
                    }
                    avgWeight = avgWeight / (weightList.size() * 1.0);
                    thematicMapNetwork.getRow(mapEdge).set(TM.avgEdgeWeightAttName.name, avgWeight);
                }
            }

        }

        //TODO consider creating 'relationship edges', ie edges between theme nodes which have member input nodes in common;  see: http://www.cgl.ucsf.edu/Research/cytoscape/groupAPI/doc/edu/ucsf/groups/GroupManager.html#regroupGroup(cytoscape.CyNode,%20boolean,%20boolean)
		return thematicMapNetwork;
	}

	
    public void getSingleNodes(CyNetwork inputNetwork, CyNetwork thematicMap, String attName) {
        Map<CyNode,CyNode> originalNodeToNewNode = new HashMap<CyNode,CyNode>();

        // find nodes that don't have a value for the attribute
        for(CyNode originalNode : inputNetwork.getNodeList()) {
            Collection<?> themeAttVals = TMUtil.getAttValues(inputNetwork, originalNode, attName);
            if (themeAttVals.isEmpty()) {
            	// create a new node in the thematic map
                CyNode newNode = ((CySubNetwork)thematicMap).addNode();
                thematicMap.getRow(newNode).set(TM.theme_member_count_att_name.name, 1);
                //add the input node to the theme node's member node list
    		    TMUtil.addToListAttribute(thematicMap.getRow(newNode), TM.memberListAttName.name, originalNode.getSUID().toString());
    		    originalNodeToNewNode.put(originalNode, newNode);
            }
        }

        // find edges connecting unassigned nodes
        for(CyEdge orig_edge : inputNetwork.getEdgeList()) {
            String interact = inputNetwork.getRow(orig_edge).get(CyEdge.INTERACTION, String.class);
            CyNode source = orig_edge.getSource();
            CyNode target = orig_edge.getTarget();

            if (originalNodeToNewNode.containsKey(source) && originalNodeToNewNode.containsKey(target)) {
            	CyNode themeSource = originalNodeToNewNode.get(source);
            	CyNode themeTarget = originalNodeToNewNode.get(target);
            	createSingleNodeEdge(inputNetwork, source, target, thematicMap, themeSource, themeTarget, interact);
            }
            else if (originalNodeToNewNode.containsKey(source)) {
            	CyNode themeSource = originalNodeToNewNode.get(source);
                Collection<?> themeAttVals = TMUtil.getAttValues(inputNetwork, target, attName);
                for (Object themeAttVal : themeAttVals) {
                	CyNode themeAttNode = themeNameToThemeNode.get(themeAttVal);
                	createSingleNodeEdge(inputNetwork, source, target, thematicMap, themeSource, themeAttNode, interact);
                }
            }
            else if (originalNodeToNewNode.containsKey(target)) {
            	CyNode themeTarget = originalNodeToNewNode.get(target);
                Collection<?> themeAttVals = TMUtil.getAttValues(inputNetwork, source, attName);
                for (Object themeAttVal : themeAttVals) {
                    CyNode themeAttNode = themeNameToThemeNode.get(themeAttVal);
                    createSingleNodeEdge(inputNetwork, source, target, thematicMap, themeAttNode, themeTarget, interact);
                }
            }
        }
    }

    
    
    private void createSingleNodeEdge(CyNetwork inputNetwork, CyNode source, CyNode target, CyNetwork thematicMap, CyNode themeSource, CyNode themeTarget, String interact) {
        CyEdge themeNodeEdge = TMUtil.getCyEdge(thematicMap, themeSource, themeTarget, CyEdge.INTERACTION, interact + "_tt");
        // if the edge does not exist, create it
        if (themeNodeEdge == null) {
            themeNodeEdge = thematicMap.addEdge(themeSource, themeTarget, false);
            CyRow row = thematicMap.getRow(themeNodeEdge);
            row.set(TM.edgeStatisticAttName.name, 0.0);
            row.set(TM.edgeSourceMemberCountAttName.name, 1);
            row.set(TM.edgeStatisticTypeAttName.name, "COUNT");
        }
        else {
            // get current count
            CyRow row = thematicMap.getRow(themeNodeEdge);
            int count = row.get(TM.edgeSourceMemberCountAttName.name, Integer.class);
            row.set(TM.edgeSourceMemberCountAttName.name, count + 1);
        }
        String themeEdgeAttributeVal = source.getSUID().toString() + "-" + target.getSUID().toString();
        TMUtil.addToListAttribute(thematicMap.getRow(themeNodeEdge), TM.edgeSourceAttName.name, themeEdgeAttributeVal);
    }
    
    
    
    /**Assigns the input graph node to the given theme node in the theme network, creating a new theme node in the theme network if one does
	 * not already exist
	 *
	 * @param inputNode the input graph node which is being assigned as belonging to a particular theme in the theme graph
	 * @param themeName the theme to which the input node belongs
	 * @param themeNameToThemeNode the map from theme names to corresponding theme nodes in the thematic network
	 * @param themeNetwork the thematic network
	 */
    private void assignNodeToTheme(CyNode inputNode, Object themeName, Map<Object,CyNode> themeNameToThemeNode, CyNetwork themeNetwork) {
        if (themeName==null) {
            return;
        }
        if (!themeNameToThemeNode.containsKey(themeName)) {
			
			CyNode themeNode = themeNetwork.addNode();
			themeNetwork.getRow(themeNode).set(CyNetwork.NAME, themeName.toString());
			
			// MKTODO what does this do? 
			//TMUtil.setStringAtt(themeNode,themeNode.getIdentifier(),themeName);

			themeNameToThemeNode.put(themeName, themeNode);
		}

		//retrieve the theme node corresponding to this theme
		CyNode themeNode = themeNameToThemeNode.get(themeName);

		//add the input node to the theme node's member node list
		TMUtil.addToListAttribute(themeNetwork.getRow(themeNode), TM.memberListAttName.name, inputNode.getSUID().toString());

	}

    private void connectThemeNodesWithConnectedInputNodes(CyNetwork thematicGraph, Map<String, Set<Object>> inputNodeIDToThemes, CyNetwork inputNetwork, CyEdge inputEdge) {
		String interaction = inputNetwork.getRow(inputEdge).get(CyEdge.INTERACTION, String.class);
		
        CyNode source = inputEdge.getSource();
		CyNode target = inputEdge.getTarget();
		Set<Object> sourceNodeThemes = inputNodeIDToThemes.get(source.getSUID().toString());
		Set<Object> targetNodeThemes = inputNodeIDToThemes.get(target.getSUID().toString());

		//if there are no source nor target themes for the nodes, their won't be any connections in the theme graph, so ignore
		if (sourceNodeThemes == null || targetNodeThemes == null || sourceNodeThemes.isEmpty() || targetNodeThemes.isEmpty())
            return;

        // check and see if edge weight attribute name has been set
        // if so, add the weight for this edge to the list of weights for the theme edge
        double inputEdgeWeight = Double.MIN_VALUE;
        if (!edgeWeightAttributeName.isEmpty()) {
        	Class<?> type = inputNetwork.getDefaultEdgeTable().getColumn(edgeWeightAttributeName).getType();
            if (type.equals(Double.class)) {
                inputEdgeWeight = inputNetwork.getRow(inputEdge).get(edgeWeightAttributeName, Double.class);
            }
            else if (type.equals(Integer.class)) {
                inputEdgeWeight = inputNetwork.getRow(inputEdge).get(edgeWeightAttributeName, Integer.class).doubleValue();
            }
        }


//    	final String themeEdgeAliasVal = interaction + "_tt";  //for theme-theme
        
		//maps input edges to the theme edges which they imply
		Map<CyEdge,Set<CyEdge>> inputEdgeToThemeEdges = new HashMap<CyEdge, Set<CyEdge>>();
		
		for(Object sourceNodeTheme : sourceNodeThemes) {
			for(Object targetNodeTheme : targetNodeThemes) {
				
                //create edge between theme nodes if it doesn't already exist
				CyNode sourceThemeNode = themeNameToThemeNode.get(sourceNodeTheme);
				CyNode targetThemeNode = themeNameToThemeNode.get(targetNodeTheme);
				
				if(allowSelfEdges || !sourceThemeNode.equals(targetThemeNode)) {
	                //TODO if we add a mode for capturing directedness, change directedness parameter in the following statements to 'true'
	                CyEdge themeEdge = TMUtil.getCyEdge(thematicGraph, sourceThemeNode, targetThemeNode, CyEdge.INTERACTION, interaction + "_tt");
	                //if the edge does not exist, create it.
	                if (themeEdge == null) {
	                    themeEdge = thematicGraph.addEdge(sourceThemeNode, targetThemeNode, false);
	                    thematicGraph.getRow(themeEdge).set(CyEdge.INTERACTION, interaction + "_tt");
	                    thematicGraph.getRow(themeEdge).set(TM.edgeStatisticAttName.name, 0.0);
	                }
	
	                //recording the input edge's source and target nodes as an attribute of the theme edge
	                String themeEdgeAttributeVal = source.getSUID().toString() + "-" + target.getSUID().toString();
	                TMUtil.addToListAttribute(thematicGraph.getRow(themeEdge), TM.edgeSourceAttName.name, themeEdgeAttributeVal);
	
	                // add the input edge weight to the list of weights (if applicable)
	                if (inputEdgeWeight!=Double.MIN_VALUE) {
	                	TMUtil.addToListAttribute(thematicGraph.getRow(themeEdge), TM.edgeWeightListAttName.name, inputEdgeWeight, Double.class);
	                }
	
	                //add new theme edge entry to the set of theme edges which this input edge corresponds to
	                Set<CyEdge> themeEdges = inputEdgeToThemeEdges.get(inputEdge);
	                if (themeEdges == null) {
	                    themeEdges = new HashSet<CyEdge>();
	                    inputEdgeToThemeEdges.put(inputEdge, themeEdges);
	                }
	                themeEdges.add(themeEdge);
				}
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
	private void addThemeToNodeThemeSet(Map<String,Set<Object>> inputNodeIDToThemes, CyNode node, Object theme) {
		if(theme == null)
			return;
		String id = node.getSUID().toString();
		Set<Object> themeSet = inputNodeIDToThemes.get(id);
		if(themeSet == null) {
			themeSet = new HashSet<Object>();
			inputNodeIDToThemes.put(id, themeSet);
		}
		themeSet.add(theme);
	}

	public CyNetworkView createThematicMapDefaultView(CyNetwork thema,String themeAttributeName, int edgeWidthType) {
		//finally, generate the graph view
		CyNetworkView themaView = networkViewFactory.createNetworkView(thema);
		
		VisualStyle visualStyle = null;
		for(VisualStyle existingStyle : visualMappingManager.getAllVisualStyles()) {
			if(existingStyle.getTitle().equals(THEME_MAP_VISUAL_STYLE)) {
				visualStyle = existingStyle;
				break;
			}
		}
		if(visualStyle == null) {
			visualStyle = createThemeMapVisualStyle(thema, edgeWidthType);
			visualMappingManager.addVisualStyle(visualStyle);
		}

		visualMappingManager.setVisualStyle(visualStyle, themaView);
		
		CyLayoutAlgorithm layout = layoutAlgorithmManager.getLayout("attribute-circle");
		if(layout == null)
			layout = layoutAlgorithmManager.getDefaultLayout();
		
        TaskIterator taskIterator = layout.createTaskIterator(themaView, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, themeAttributeName);
        taskManager.execute(taskIterator);
        
		themaView.updateView();
		networkViewManager.addNetworkView(themaView);
		
		return themaView;
	}


	private VisualStyle createThemeMapVisualStyle(CyNetwork network, int edgeWidthType) {
        // Loop through the nodes in the network to find the max and min member counts
        int maxMemberCount = Integer.MIN_VALUE;
        int minMemberCount = Integer.MAX_VALUE;

        for(CyNode node: network.getNodeList()) {
            int count = network.getRow(node).get(TM.theme_member_count_att_name.name, Integer.class);
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

        for(CyEdge edge : network.getEdgeList()) {
            double count;
            if (edgeWidthType == EDGE_WIDTH_STATISTICS) {
                count = network.getRow(edge).get(TM.edgeStatisticAttName.name, Double.class);
            }
            else {
                count = network.getRow(edge).get(TM.edgeSourceMemberCountAttName.name, Integer.class).doubleValue();
            }
            if (count>maxEdgeWidthAtt) {
                maxEdgeWidthAtt = count;
            }
            if (count<minEdgeWidthAtt) {
                minEdgeWidthAtt = count;
            }
            String type = network.getRow(edge).get(CyEdge.INTERACTION, String.class);
            if (!edgeTypes.contains(type)) {
                edgeTypes.add(type);
            }
        }

		VisualStyle visualStyle = visualStyleFactory.createVisualStyle(THEME_MAP_VISUAL_STYLE);
		
        // Set default shape and color
        visualStyle.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
        visualStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(255,150,150));
        visualStyle.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, new Color(204,204,255));
        
        // Passthrough Mapping - set node label
        VisualMappingFunction<String,String> pm = 
        	functionFactoryPassthrough.createVisualMappingFunction(TM.formattedNameAttributeName.name, String.class, BasicVisualLexicon.NODE_LABEL);
        visualStyle.addVisualMappingFunction(pm);

        //  Continuous Mapping - set node size (max = 100, min = 10)
        ContinuousMapping<Integer,Double> cm = (ContinuousMapping<Integer,Double>)
        	functionFactoryContinuous.createVisualMappingFunction(TM.theme_member_count_att_name.name, Integer.class, BasicVisualLexicon.NODE_SIZE);
        cm.addPoint(minMemberCount, new BoundaryRangeValues<Double>(10d, 10d, 10d));
        cm.addPoint(maxMemberCount, new BoundaryRangeValues<Double>(100d,100d,100d));
        visualStyle.addVisualMappingFunction(cm);
        
        // Discrete Mapping - set edge color
		DiscreteMapping<String,Paint> dm = (DiscreteMapping<String,Paint>)
			functionFactoryDiscrete.createVisualMappingFunction(CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		
        ColorGenerator colorGenerator = new ColorGenerator();
        for(String type : edgeTypes) {
        	dm.putMapValue(type, colorGenerator.nextColor());
        }
        visualStyle.addVisualMappingFunction(dm);

        //  Continuous Mapping - set edge width (max = 10, min = 1)
        if(edgeWidthType == EDGE_WIDTH_STATISTICS) {
        	 ContinuousMapping<Double,Double> cem = (ContinuousMapping<Double,Double>)
        	        functionFactoryContinuous.createVisualMappingFunction(TM.edgeStatisticAttName.name, Double.class, BasicVisualLexicon.EDGE_WIDTH);
        	 cem.addPoint(minEdgeWidthAtt, new BoundaryRangeValues<Double>(1d, 1d, 1d));
             cem.addPoint(maxEdgeWidthAtt, new BoundaryRangeValues<Double>(10d,10d,10d));
             visualStyle.addVisualMappingFunction(cem);
        }
        else {
        	ContinuousMapping<Integer,Double> cem = (ContinuousMapping<Integer,Double>)
    	        	functionFactoryContinuous.createVisualMappingFunction(TM.edgeSourceMemberCountAttName.name, Integer.class, BasicVisualLexicon.EDGE_WIDTH);
        	cem.addPoint((int)Math.round(minEdgeWidthAtt), new BoundaryRangeValues<Double>(1d, 1d, 1d));
        	cem.addPoint((int)Math.round(maxEdgeWidthAtt), new BoundaryRangeValues<Double>(10d, 10d, 10d));
        	visualStyle.addVisualMappingFunction(cem);
        }
        
        return visualStyle;
	}

	

    private String formatName(String name) {
        int curLength = 0;
        String newName = "";
        int maxSize = 15;

        Pattern pattern = Pattern.compile("[ \t\n\f\r]");
        Matcher matcher = pattern.matcher(name);
        int index = 0;
        while (matcher.find(index)) {
            String word = name.substring(index, matcher.start());
            String whiteSpace = name.substring(matcher.start(), matcher.end());

            if (curLength + word.length() + whiteSpace.length() < maxSize) {
                newName = newName + word + whiteSpace;
                curLength = curLength + word.length() + whiteSpace.length();
            }
            else if (curLength + word.length() < maxSize) {
                newName = newName + word + "\n";
                curLength = 0;
            }
            else {
                newName = newName + "\n" + word + whiteSpace;
                curLength = word.length() + whiteSpace.length();
            }

            index = matcher.end();
        }
        String lastWord = name.substring(index);
        if (curLength + lastWord.length() > maxSize) {
            newName = newName + "\n" + lastWord;
        }
        else {
            newName = newName + lastWord;
        }

        return newName;

    }

}

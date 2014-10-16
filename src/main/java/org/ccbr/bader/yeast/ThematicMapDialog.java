package org.ccbr.bader.yeast;

/**
 * Created by IntelliJ IDEA.
 * User: lmorrison
 * Date: Apr 28, 2008
 * Time: 4:28:43 PM
 * To change this template use File | Settings | File Templates.
 */

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.ccbr.bader.yeast.view.gui.misc.JButtonMod;
import org.ccbr.bader.yeast.view.gui.misc.JLabelMod;

import com.google.inject.Inject;


public class ThematicMapDialog extends JDialog implements PropertyChangeListener, ActionListener {


    // Variables declaration - do not modify
    private JButton cancelButton;
    private JButton createMapButton;
    private JLabel attributeListLabel;
    private JScrollPane attributeListScrollPane;
    private JComboBox attributeListComboBox;
    private JCheckBox selfLoopCheckBox;
    private JCheckBox includeSingleNodesCheckBox;    
    private JRadioButton noStatisticRadioButton;
    private JRadioButton shuffleStatisticRadioButton;
    private JTextField shuffleStatisticTextField;
    private JRadioButton hyperGeoProbabilityStatisticRadioButton;
    private JRadioButton cumulativeHGProbabilityStatisticRadioButton;
    private JPanel statisticsPanel;
    private JRadioButton edgeWidthCountRadioButton;
    private JRadioButton edgeWidthStatisticRadioButton;
    private JPanel edgeWidthPanel;
    private JCheckBox edgeWeightCheckBox;
    private JComboBox edgeWeightAttributeListComboBox;
    private JPanel edgeWeightPanel;
    private JCheckBox evaluateShuffleCheckBox;
    private JTextField evaluateShuffleFlagsTextField;
    private JButton evaluateShuffleChooseFileButton;
    private JLabel evaluateShuffleFileLabel;
    private JPanel evaluateShufflePanel;

    private File evaluateShuffleFile = null;

    
    @Inject
    public ThematicMapDialog(Frame parent) {
	    super(parent, true);
        this.setTitle("Create Thematic Map Dialog");

        initComponents();
		//updateComponents();
	}

    /**
	 * Listening to local signals used among Swing components in this dialog.
	 */
	public void propertyChange(PropertyChangeEvent evt) {

	}

    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButtonMod("Cancel");
            cancelButton.addActionListener(this);
        }
        return cancelButton;
    }

    private JButton getCreateMapButton() {
        if (createMapButton == null) {
            createMapButton = new JButtonMod("Create Thematic Map");
            createMapButton.addActionListener(this);
        }
        return createMapButton;
    }

    private JScrollPane getAttributeListScrollPane() {
        if (attributeListScrollPane == null) {
            attributeListScrollPane = new JScrollPane();
            attributeListScrollPane.setToolTipText("List of available attributes");
            attributeListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            // Create JList item for scroll pane
            JList attributeList = new JList(getAttributes());
            attributeList.setVisibleRowCount(4);
            attributeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            attributeListScrollPane.getViewport().setView(attributeList);
        }
        return attributeListScrollPane;
    }

    private JComboBox getAttributeListComboBox()  {
		if (attributeListComboBox !=null) return attributeListComboBox;

		//create the new combo box
		attributeListComboBox = new JComboBox();
		attributeListComboBox.setMaximumSize(new Dimension(60,15));

        // set 1st item as '[Select Attribute Name]' and set to default
        attributeListComboBox.addItem("[Select Attribute Name]");
        attributeListComboBox.setSelectedIndex(0);

        String[] attributeList = getAttributes();
        if ((attributeList != null) && attributeList.length > 0) {
            for (String attName : attributeList) {
                attributeListComboBox.addItem(attName);
            }
        }

		return attributeListComboBox;
	}



    private JLabel getAttributeListLabel() {
        if (attributeListLabel == null) {
            attributeListLabel = new JLabelMod("Thematic Map Attribute:");
        }
        return attributeListLabel;
    }

    private JCheckBox getSelfLoopCheckBox() {
        if (selfLoopCheckBox == null) {
            selfLoopCheckBox = new JCheckBox("Hide self loops", false);
        }
        return selfLoopCheckBox;
    }

    private JCheckBox getIncludeSingleNodesCheckBox() {
        if (includeSingleNodesCheckBox == null) {
            includeSingleNodesCheckBox = new JCheckBox("Include single nodes from original network", false);
        }
        return includeSingleNodesCheckBox;
    }
    
    private JRadioButton getNoStatisticRadioButton() {
        if (noStatisticRadioButton == null) {
            noStatisticRadioButton = new JRadioButton("None");
            noStatisticRadioButton.setSelected(true);
            noStatisticRadioButton.addActionListener(this);
        }
        return noStatisticRadioButton;
    }

    private JRadioButton getShuffleStatisticRadioButton() {
        if (shuffleStatisticRadioButton == null) {
            shuffleStatisticRadioButton = new JRadioButton("Shuffle");
            shuffleStatisticRadioButton.addActionListener(this);
        }
        return shuffleStatisticRadioButton;
    }

    private JTextField getShuffleStatisticTextField() {
        if (shuffleStatisticTextField == null) {
            shuffleStatisticTextField = new JFormattedTextField("1000");
            shuffleStatisticTextField.setEnabled(shuffleStatisticRadioButton.isSelected());
        }
        return shuffleStatisticTextField;
    }

    private JRadioButton getHyperGeoProbabilityStatisticRadioButton() {
        if (hyperGeoProbabilityStatisticRadioButton == null) {
            hyperGeoProbabilityStatisticRadioButton = new JRadioButton("Hypergeometric Probability");
            hyperGeoProbabilityStatisticRadioButton.addActionListener(this);
        }
        return hyperGeoProbabilityStatisticRadioButton;
    }

    private JRadioButton getCumulativeHGProbabilityStatisticRadioButton() {
        if (cumulativeHGProbabilityStatisticRadioButton == null) {
            cumulativeHGProbabilityStatisticRadioButton = new JRadioButton("Hypergeometric Probability (cumulative)");
            cumulativeHGProbabilityStatisticRadioButton.addActionListener(this);
        }
        return cumulativeHGProbabilityStatisticRadioButton;
    }
    private JPanel getStatisticsPanel() {

        if (statisticsPanel == null) {
            // Create button group for radio buttons
            ButtonGroup statisticGroup = new ButtonGroup();
            statisticGroup.add(getNoStatisticRadioButton());
            statisticGroup.add(getShuffleStatisticRadioButton());
            statisticGroup.add(getHyperGeoProbabilityStatisticRadioButton());
            statisticGroup.add(getCumulativeHGProbabilityStatisticRadioButton());

            statisticsPanel = new JPanel(new GridBagLayout());
            statisticsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));

            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.weightx=0.5;
            c.gridwidth = 1;
            c.gridx=0;
		    c.gridy=0;
            statisticsPanel.add(getNoStatisticRadioButton(),c);

            c.gridwidth = 1;
            c.gridx=0;
		    c.gridy=1;
            statisticsPanel.add(getShuffleStatisticRadioButton(),c);

            c.anchor = GridBagConstraints.LINE_END;
            c.weightx=0.5;
            c.gridx=1;
            c.gridy=1;
            statisticsPanel.add(getShuffleStatisticTextField(),c);

            c.anchor = GridBagConstraints.LINE_START;
            c.weightx=0.5;
            c.gridwidth = 1;
            c.gridx=0;
		    c.gridy=2;
            statisticsPanel.add(getHyperGeoProbabilityStatisticRadioButton(),c);

            c.anchor = GridBagConstraints.LINE_START;
            c.weightx=0.5;
            c.gridwidth = 1;
            c.gridx=0;
		    c.gridy=3;
            statisticsPanel.add(getCumulativeHGProbabilityStatisticRadioButton(),c);
        }
        return statisticsPanel;
    }

    private JRadioButton getEdgeWidthCountRadioButton() {
        if (edgeWidthCountRadioButton == null) {
            edgeWidthCountRadioButton = new JRadioButton("Count");
            edgeWidthCountRadioButton.setSelected(true);
            edgeWidthCountRadioButton.addActionListener(this);
        }
        return edgeWidthCountRadioButton;
    }

    private JRadioButton getEdgeWidthStatisticRadioButton() {
        if (edgeWidthStatisticRadioButton == null) {
            edgeWidthStatisticRadioButton = new JRadioButton("Statistics");
            edgeWidthStatisticRadioButton.addActionListener(this);
            edgeWidthStatisticRadioButton.setEnabled(false);
        }
        return edgeWidthStatisticRadioButton;
    }

    private JPanel getEdgeWidthPanel() {

        if (edgeWidthPanel == null) {

            // Create button group for radio buttons
            ButtonGroup edgeWidthGroup = new ButtonGroup();
            edgeWidthGroup.add(getEdgeWidthCountRadioButton());
            edgeWidthGroup.add(getEdgeWidthStatisticRadioButton());

            edgeWidthPanel = new JPanel(new GridBagLayout());
            edgeWidthPanel.setBorder(BorderFactory.createTitledBorder("Edge Width Options"));

            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.weightx=0.5;
            c.gridwidth = 1;
            c.gridx=0;
		    c.gridy=0;
            edgeWidthPanel.add(getEdgeWidthCountRadioButton(),c);

            c.gridwidth = 1;
            c.gridx=0;
		    c.gridy=1;
            edgeWidthPanel.add(getEdgeWidthStatisticRadioButton(),c);
        }
        return edgeWidthPanel;
    }

    private JCheckBox getEdgeWeightCheckBox() {
        if (edgeWeightCheckBox == null) {
            edgeWeightCheckBox = new JCheckBox("Use Edge Weights", false);
            edgeWeightCheckBox.addActionListener(this);
        }
        return edgeWeightCheckBox;
    }

    private JComboBox getEdgeWeightAttributeListComboBox()  {
		if (edgeWeightAttributeListComboBox !=null) {
            return edgeWeightAttributeListComboBox;
        }

		//create the new combo box
		edgeWeightAttributeListComboBox = new JComboBox();
		edgeWeightAttributeListComboBox.setMaximumSize(new Dimension(60,15));

        // set 1st item as '[Select Attribute Name]' and set to default
        edgeWeightAttributeListComboBox.addItem("[Select Edge Weight Attribute Name]");
        edgeWeightAttributeListComboBox.setSelectedIndex(0);

        String[] attributeList = getNumericalEdgeAttributes();
        if ((attributeList != null) && attributeList.length > 0) {
            for (String attName : attributeList) {
                edgeWeightAttributeListComboBox.addItem(attName);
            }
        }

        edgeWeightAttributeListComboBox.setEnabled(false);

        return edgeWeightAttributeListComboBox;
	}


    private JPanel getEdgeWeightPanel() {

        if (edgeWeightPanel == null) {

            edgeWeightPanel = new JPanel(new GridBagLayout());
            edgeWeightPanel.setBorder(BorderFactory.createTitledBorder("Edge Weight Options"));

            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.weightx=0.5;
            c.gridwidth = 1;
            c.gridx=0;
		    c.gridy=0;
            edgeWeightPanel.add(getEdgeWeightCheckBox(),c);

            c.gridwidth = 1;
            c.gridx=0;
		    c.gridy=1;
            edgeWeightPanel.add(getEdgeWeightAttributeListComboBox(),c);
        }
        return edgeWeightPanel;
    }

    private JCheckBox getEvaluateShuffleCheckBox() {
        if (evaluateShuffleCheckBox == null) {
            evaluateShuffleCheckBox = new JCheckBox("Evaluate Shuffle Iterations", false);
            evaluateShuffleCheckBox.addActionListener(this);
            evaluateShuffleCheckBox.setEnabled(false);
        }
        return evaluateShuffleCheckBox;
    }

    private JTextField getEvaluateShuffleFlagsTextField() {
        if (evaluateShuffleFlagsTextField == null) {
            evaluateShuffleFlagsTextField = new JFormattedTextField("10,50,100,500,1000");
            evaluateShuffleFlagsTextField.setEnabled(evaluateShuffleCheckBox.isSelected());
            evaluateShuffleFlagsTextField.setToolTipText("Comma separated list of flag levels for shuffle evaluation");
        }
        return evaluateShuffleFlagsTextField;
    }

    private JButton getEvaluateShuffleChooseFileButton() {
        if (evaluateShuffleChooseFileButton == null) {
            evaluateShuffleChooseFileButton = new JButtonMod("Select Save Directory");
            evaluateShuffleChooseFileButton.setEnabled(evaluateShuffleCheckBox.isSelected());
            evaluateShuffleChooseFileButton.addActionListener(this);
        }
        return evaluateShuffleChooseFileButton;
    }

    private JLabel getEvaluateShuffleFileLabel() {
        if (evaluateShuffleFileLabel == null) {
            evaluateShuffleFileLabel = new JLabel("<No Directory Selected>");
            evaluateShuffleFileLabel.setEnabled(evaluateShuffleCheckBox.isSelected());
        }
        return evaluateShuffleFileLabel;
    }

    private JPanel getEvaluateShufflePanel() {
        if (evaluateShufflePanel == null) {
            evaluateShufflePanel = new JPanel(new GridBagLayout());
            evaluateShufflePanel.setBorder(BorderFactory.createTitledBorder("Evaluate Shuffle Iterations"));

            evaluateShufflePanel.setEnabled(false);
            
            evaluateShufflePanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.weightx=0.5;
            c.gridwidth = 2;
            c.gridx=0;
		    c.gridy=0;
            evaluateShufflePanel.add(getEvaluateShuffleCheckBox(),c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.weightx=0.5;
            c.gridwidth = 2;
            c.gridx=0;
		    c.gridy=1;
            evaluateShufflePanel.add(getEvaluateShuffleFlagsTextField(),c);

            c.weightx=0.5;
            c.gridwidth = 1;
            c.gridx=0;
		    c.gridy=2;
            evaluateShufflePanel.add(getEvaluateShuffleChooseFileButton(),c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.weightx=0.5;
            c.gridwidth = 1;
            c.gridx=1;
		    c.gridy=2;
            evaluateShufflePanel.add(getEvaluateShuffleFileLabel(),c);


        }
        return evaluateShufflePanel;
    }

    public void actionPerformed(ActionEvent event) {
//        Object src = event.getSource();
//        if (src instanceof JRadioButton) {
//            shuffleStatisticTextField.setEnabled(shuffleStatisticRadioButton.isSelected());
//            evaluateShuffleCheckBox.setEnabled(shuffleStatisticRadioButton.isSelected());
//
//            edgeWidthStatisticRadioButton.setEnabled(shuffleStatisticRadioButton.isSelected() || hyperGeoProbabilityStatisticRadioButton.isSelected() || cumulativeHGProbabilityStatisticRadioButton.isSelected());
//            if (!edgeWidthStatisticRadioButton.isEnabled() && edgeWidthStatisticRadioButton.isSelected()) {
//                edgeWidthCountRadioButton.setSelected(true);
//            }
//        }
//        else if (src instanceof JCheckBox) {
//            JCheckBox cbsrc = (JCheckBox) src;
//            if (cbsrc.equals(edgeWeightCheckBox)) {
//                edgeWeightAttributeListComboBox.setEnabled(edgeWeightCheckBox.isSelected());
//            }
//            else if (cbsrc.equals(evaluateShuffleCheckBox)) {
//                evaluateShuffleFlagsTextField.setEnabled(evaluateShuffleCheckBox.isSelected());
//                evaluateShuffleChooseFileButton.setEnabled(evaluateShuffleCheckBox.isSelected());
//                evaluateShuffleFileLabel.setEnabled(evaluateShuffleCheckBox.isSelected());    
//            }
//        }
//        else if (src instanceof JButton) {
//            JButton bsrc = (JButton) src;
//            if (bsrc.equals(cancelButton)) {  // Cancel
//                dispose();
//            }
//            else if (bsrc.equals(evaluateShuffleChooseFileButton)) {  // choose save file for shuffle evaluations
//                JFileChooser chooser = new JFileChooser();
//                //chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//                int retval = chooser.showSaveDialog(this);
//
//                if (retval == JFileChooser.APPROVE_OPTION) {
//				    evaluateShuffleFile = chooser.getSelectedFile();
//                    System.out.println("evaluateFile at dialog level: " + evaluateShuffleFile);
//                    evaluateShuffleFileLabel.setText(evaluateShuffleFile.getAbsolutePath());
//                }
//            }
//            else if (bsrc.equals(createMapButton)) { // Create thematic Map
//
//                if (attributeListComboBox.getSelectedIndex()<=0) {
//                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Select an attribute before creating thematic map.","Error",JOptionPane.ERROR_MESSAGE);
//                    System.out.println("You must select an attribute before creating the map");
//                }
//                else if (evaluateShuffleCheckBox.isSelected() && evaluateShuffleFile==null) {
//                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Select a save file for the shuffle evaluations, or disable the evaluations","Error",JOptionPane.ERROR_MESSAGE);
//                }
//                else {
//                    String attName = (String) attributeListComboBox.getSelectedItem();
//                    ThematicMap tmap = new ThematicMap();
//
//                    // determine whether edge weights should be read and stored
//                    if (edgeWeightCheckBox.isSelected() && edgeWeightAttributeListComboBox.getSelectedIndex()>0) {
//                        String edgeWeightAttributeName = (String) edgeWeightAttributeListComboBox.getSelectedItem();
//                        //ThematicMapFunctionPrototype.setEdgeWeightAttributeName(edgeWeightAttributeName);
//                        tmap.setEdgeWeightAttributeName(edgeWeightAttributeName);
//                    }
//
//                    CyNetwork inputNetwork = Cytoscape.getCurrentNetwork();
//                    //CyNetwork thematicMap = ThematicMapFunctionPrototype.createThematicMap(inputNetwork, attName);
//                    CyNetwork thematicMap = tmap.createThematicMap(inputNetwork, attName);
//
//                   // if includeSingleNode check box is checked, loop through the original network and add the nodes that don't have any associated attributes
//                    Set<Node> single_nodes = new HashSet<Node>();
//                    Set<Edge> single_node_edges = new HashSet<Edge>();
//
//
//
//                    if (shuffleStatisticRadioButton.isSelected()) {
//
//                        int[] allFlagValues = null;
//
//                        try {
//
//                            // check to see if will be performing evaluation, and get flag levels
//                            if (evaluateShuffleCheckBox.isSelected()) {
//                                String flagList = evaluateShuffleFlagsTextField.getText();
//                                String[] allFlags = flagList.split(",");
//
//                                allFlagValues = new int[allFlags.length];
//                                for (int i=0; i<allFlags.length; i++) {
//                                    allFlagValues[i] = Integer.parseInt(allFlags[i]);
//                                }
//                            }
//
//
//                        }
//                        catch (NumberFormatException e) {
//                            JOptionPane.showMessageDialog(this,"Invalid values for evaluation flags.  Flags must be valid integers.  No evaluation will be conducted.", "Error", JOptionPane.ERROR_MESSAGE);
//                            allFlagValues = null;
//                        }
//
//                        try {
//                            int shuffleIterations = Integer.parseInt(shuffleStatisticTextField.getText());
//
//                            long timeBefore = System.currentTimeMillis();
//                            //NetworkShuffleStatistic stat = new NetworkShuffleStatistic(inputNetwork,thematicMap);
//                            //stat.getStatisticsProgressBar(attName, shuffleIterations, allFlagValues, evaluateShuffleFile);
//
//                            NetworkShuffleStatisticMultiThreaded statThreaded = new NetworkShuffleStatisticMultiThreaded(inputNetwork, thematicMap);
//                            statThreaded.getStatistics(attName, shuffleIterations, allFlagValues, evaluateShuffleFile);
//                            long timeAfter = System.currentTimeMillis();
//
//                            //System.out.println("time elapsed: " + (timeAfter-timeBefore)/1000.0);
//
//                        }
//                        catch (NumberFormatException e) {
//                            JOptionPane.showMessageDialog(this,"Invalid value '" + shuffleStatisticTextField.getText() + "' for shuffle iterations - must be a valid integer. Statistics will not be calculated.", "Error", JOptionPane.ERROR_MESSAGE);
//                            shuffleStatisticTextField.setText("1000");
//                        }                       
//
//                    }
//                    else if (hyperGeoProbabilityStatisticRadioButton.isSelected()) {
//                        HyperGeomProbabilityStatistic stat = new HyperGeomProbabilityStatistic(inputNetwork, thematicMap);
//                        stat.getStatistics(attName, false);
//                    }
//                    else if (cumulativeHGProbabilityStatisticRadioButton.isSelected()) {
//                        HyperGeomProbabilityStatistic stat = new HyperGeomProbabilityStatistic(inputNetwork, thematicMap);
//                        stat.getStatistics(attName, true);
//                    }
//                    else {
//                        // Loop through edges and copy the count attribute to the statistic attribute, and update the statistic type attribute
//
//                    }
//
//                    int edgeWidthType;
//                    if (edgeWidthStatisticRadioButton.isSelected()) {
//                        //edgeWidthType = ThematicMapFunctionPrototype.EDGE_WIDTH_STATISTICS;
//                        edgeWidthType = ThematicMap.EDGE_WIDTH_STATISTICS;
//                    }
//                    else {
//                        //edgeWidthType = ThematicMapFunctionPrototype.EDGE_WIDTH_COUNT;
//                        edgeWidthType = ThematicMap.EDGE_WIDTH_COUNT;
//
//                    }
//
//                    if (includeSingleNodesCheckBox.isSelected()) {
//                        tmap.getSingleNodes(inputNetwork, thematicMap, attName, single_nodes, single_node_edges);
//
//                    }
//
//                    //ThematicMapFunctionPrototype.createThematicMapDefaultView(thematicMap, attName, edgeWidthType);
//                    tmap.createThematicMapDefaultView(thematicMap, attName, edgeWidthType);
//
//                    if (selfLoopCheckBox.isSelected()) {
//                        // loop through network edges to find self loops
//                        Iterator edges_i = thematicMap.edgesIterator();
//                        CyNetworkView thematicMapView = Cytoscape.getNetworkView(thematicMap.getIdentifier());
//
//                        while (edges_i.hasNext()) {
//                            Edge edge = (Edge) edges_i.next();
//                            if (edge.getSource().equals(edge.getTarget())) {
//                                EdgeView edgeView = thematicMapView.getEdgeView(edge);
//                                thematicMapView.hideGraphObject(edgeView);
//                            }
//                        }
//                        thematicMapView.updateView();
//
//                    }
//
//                    dispose();
//                }               
//            }
//        }
    }

    private void initComponents() {

        // Create button group for radio buttons
        ButtonGroup statisticGroup = new ButtonGroup();
        statisticGroup.add(getNoStatisticRadioButton());
        statisticGroup.add(getShuffleStatisticRadioButton());

        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.LINE_START;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        this.add(getAttributeListLabel(), c);

        //c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        //c.weightx = 1;
        c.gridwidth = 2;
        c.gridx=0;
		c.gridy=1;
        //this.add(getAttributeListScrollPane(),c);
        this.add(getAttributeListComboBox(),c);

        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 2;
        c.gridx=0;
        c.gridy = 2;
        this.add(getSelfLoopCheckBox(),c);

        c.gridx = 0;
        c.gridy = 3;
        this.add(getIncludeSingleNodesCheckBox(),c);

         c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx=0;
		c.gridy=4;
        this.add(getStatisticsPanel(),c);

        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx=0;
		c.gridy=5;
        this.add(getEdgeWidthPanel(),c);

        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx=0;
		c.gridy=6;
        this.add(getEdgeWeightPanel(),c);

        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx=0;
		c.gridy=7;
        this.add(getEvaluateShufflePanel(),c);

        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy=8;
        this.add(getCancelButton(),c);

        c.gridx=1;
        c.gridy=8;
        this.add(getCreateMapButton(),c);

        pack();
    }

    private String[] getNumericalEdgeAttributes() {

        String[] emptyList = {};

//        // get the attributes
//        CyAttributes attributes = Cytoscape.getEdgeAttributes();
//
//        // if there aren't any attributes, return an empty list
//        if (attributes == null) {
//            return emptyList;
//        } else {
//
//            // / get the list of attributes in a String array. If the array
//            // is null or empty, return an empty list
//            String attributeNames[] = attributes.getAttributeNames();
//
//            // get the list of numerical attributes (integer or double)
//            Set<String> numericalAttributes = new HashSet<String>();
//
//            if (attributeNames == null || attributeNames.length == 0) {
//                return emptyList;
//            } else {
//                for (String attributeName: attributeNames) {
//                    Byte type = attributes.getType(attributeName);
//                    if (type.equals(CyAttributes.TYPE_INTEGER) || type.equals(CyAttributes.TYPE_FLOATING)) {
//                        numericalAttributes.add(attributeName);
//                    }
//
//                }
//                int numNumericalAttributes = numericalAttributes.size();
//                if (numNumericalAttributes > 0) {
//
//                    String[] numericalAttributesArray = new String [numNumericalAttributes];
//
//                    return numericalAttributes.toArray(numericalAttributesArray);
//                }
//                else {
//                    return emptyList;
//                }
//            }
//
//        }
return emptyList;
    }

    private String[] getAttributes() {

        String[] emptyList = {};
//
//        // get the attributes
//        CyAttributes attributes = Cytoscape.getNodeAttributes();
//
//        // if there aren't any attributes, call getNewNoAttributesMenuItem()
//        if (attributes == null) {
//            return emptyList;
//        } else {
//            // get the list of attributes in a String array. If the array
//            // is null or empty, call getNewNoAttributesMenuItem()
//            String attributeNames[] = attributes.getAttributeNames();
//            if (attributeNames == null || attributeNames.length == 0) {
//                return emptyList;
//            } else {
//                return attributeNames;
//            }
//
//        }
        return emptyList;
    }
}

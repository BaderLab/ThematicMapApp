package org.ccbr.bader.yeast;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;


@SuppressWarnings("serial")
public class CreateThematicMapDialog extends JDialog {

	private final String NONE = "[None]";
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private Provider<ThematicMapTask> thematicMapTaskProvider;
	@Inject private DialogTaskManager taskManager;
	

	private JRadioButton countRadio;
	private JRadioButton statisticsRadio;
	private JRadioButton noneRadio;
	private JRadioButton hyperRadio;
	private JRadioButton culmRadio;
	private JRadioButton shuffRadio;
	
	private JComboBox<String> attributeCombo;
	private JComboBox<String> weightCombo;
	
	private JCheckBox removeSelfEdges;
	private JCheckBox singleNodesCheck;
	private JCheckBox evaluateCheck;
	
	private JTextField evaluateText;
	private JTextField iterationsText;
	
	private File evaluateFile = null;
	private String networkName;
	
	private Set<Component> shuffleEnablement = new HashSet<Component>();
	private Set<Component> evaluateEnablement = new HashSet<Component>();
	
	
	
	@Inject
	public CreateThematicMapDialog(CySwingApplication application, CyApplicationManager applicationManager) {
		super(application.getJFrame(), true);
		CyNetwork inputNetwork = applicationManager.getCurrentNetwork();
		this.networkName = inputNetwork.getRow(inputNetwork).get(CyNetwork.NAME, String.class);
		setTitle("Create Thematic Map: " + networkName);
		this.applicationManager = applicationManager;
		createContents();
		updateEnablement();
	}
	
	
	private void createContents() {
		JPanel parent = new JPanel();
		parent.setLayout(new BorderLayout());
		parent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(parent);
		
		JPanel attributePanel = createAttributePanel();
		JPanel statisticsPanel = createStatisticsPanel();
		JPanel buttonPanel = createButtonPanel();
		
		statisticsPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		
		parent.add(attributePanel, BorderLayout.WEST);
		parent.add(statisticsPanel, BorderLayout.CENTER);
		parent.add(buttonPanel, BorderLayout.SOUTH);
		
		pack();
		setMinimumSize(getPreferredSize());
	}
	
	
	private JPanel createAttributePanel() {
		JPanel attributePanel = new JPanel();
		attributePanel.setLayout(new BoxLayout(attributePanel, BoxLayout.Y_AXIS));
		
		JLabel label = new JLabel("Attribute");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		attributePanel.add(label);
		
		attributeCombo = new JComboBox<String>();
		for(String attribute : getAttributes(true)) {
			attributeCombo.addItem(attribute);
		}
		attributeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
		attributeCombo.setPreferredSize(new Dimension(220, attributeCombo.getPreferredSize().height));
		attributeCombo.setMaximumSize(new Dimension(220, attributeCombo.getPreferredSize().height));
		attributePanel.add(attributeCombo);
		
		attributePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		label = new JLabel("Edge Weight Attribute");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		attributePanel.add(label);
		
		weightCombo = new JComboBox<String>();
		weightCombo.addItem(NONE);
		for(String attribute : getAttributes(false)) {
			weightCombo.addItem(attribute);
		}
		weightCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
		weightCombo.setPreferredSize(new Dimension(220, weightCombo.getPreferredSize().height));
		weightCombo.setMaximumSize(new Dimension(220, weightCombo.getPreferredSize().height));
		attributePanel.add(weightCombo);
		
		attributePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		label = new JLabel("Edge Width");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		attributePanel.add(label);
		
		countRadio = new JRadioButton("Count");
		statisticsRadio = new JRadioButton("Statistics");
		groupButtons(countRadio, statisticsRadio);
		
		ActionListener radioListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEnablement();
			}
		};
		
		countRadio.addActionListener(radioListener);
		statisticsRadio.addActionListener(radioListener);
		
		attributePanel.add(countRadio);
		attributePanel.add(statisticsRadio);
		
		attributePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		label = new JLabel("Options");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		attributePanel.add(label);
		
		removeSelfEdges = new JCheckBox("Remove self edges");
		singleNodesCheck = new JCheckBox("Include single nodes");
		
		attributePanel.add(removeSelfEdges);
		attributePanel.add(singleNodesCheck);
		
		return attributePanel;
	}
	
	
	private JPanel createStatisticsPanel() {
		JPanel statisticsPanel = new JPanel();
		statisticsPanel.setLayout(new BorderLayout());
		
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
		JLabel label = new JLabel("Statistics");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		radioPanel.add(label);
		
		noneRadio  = new JRadioButton("None");
		hyperRadio = new JRadioButton("Hypergeometric Probability");
		culmRadio  = new JRadioButton("Hypergeometric Probability (Cumulative)");
		shuffRadio = new JRadioButton("Shuffle"); 
		
		groupButtons(noneRadio, hyperRadio, culmRadio, shuffRadio);
		
		ActionListener radioListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEnablement();
			}
		};
		
		noneRadio.addActionListener(radioListener);
		hyperRadio.addActionListener(radioListener);
		culmRadio.addActionListener(radioListener);
		shuffRadio.addActionListener(radioListener);
		
		radioPanel.add(noneRadio);
		radioPanel.add(hyperRadio);
		radioPanel.add(culmRadio);
		radioPanel.add(shuffRadio);
		
		statisticsPanel.add(radioPanel, BorderLayout.NORTH);
		
		JPanel shufflePanel = createShufflePanel();
		statisticsPanel.add(shufflePanel, BorderLayout.CENTER);
		
		return statisticsPanel;
	}
	
	private JPanel createShufflePanel() {
		JPanel shufflePanel = new JPanel();
		shufflePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		GridBagConstraints gbc;
		shufflePanel.setLayout(new GridBagLayout());
		shufflePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		
		JPanel iterationsPanel = new JPanel();
		iterationsPanel.setLayout(new BoxLayout(iterationsPanel, BoxLayout.X_AXIS));
		JLabel label = new JLabel("Iterations:");
		iterationsPanel.add(label);
		iterationsText = new JFormattedTextField("1000");
		iterationsPanel.add(iterationsText);
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		shufflePanel.add(iterationsPanel, gbc);
		
		evaluateCheck = new JCheckBox("Evaluate shuffle iterations");
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		shufflePanel.add(evaluateCheck, gbc);

		evaluateText = new JFormattedTextField("10,50,100,500,1000");
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 2;
		shufflePanel.add(evaluateText, gbc);
		
		JLabel saveLabel = new JLabel("Save Directory");
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 3;
		shufflePanel.add(saveLabel, gbc);
		
		JPanel browsePanel = createBrowsePanel();
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weighty = 1.0;
		shufflePanel.add(browsePanel, gbc);
		
		evaluateCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEnablement();
			}
		});
		
		Collections.<Component>addAll(shuffleEnablement, label, iterationsText, evaluateCheck);
		Collections.<Component>addAll(evaluateEnablement, evaluateText, saveLabel);
		
		return shufflePanel;
	}
	
	
	private JPanel createBrowsePanel() {
		JPanel browsePanel = new JPanel(new BorderLayout());
		final JTextField directoryText = new JTextField();
		directoryText.setEditable(false);
		browsePanel.add(directoryText, BorderLayout.CENTER);
		JButton browseButton = new JButton("Browse...");
		browsePanel.add(browseButton, BorderLayout.EAST);
		
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
		        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		        int retval = chooser.showSaveDialog(CreateThematicMapDialog.this);
		        if (retval == JFileChooser.APPROVE_OPTION) {
				    File selectedFile = chooser.getSelectedFile();
		            directoryText.setText(selectedFile.getPath());
		            evaluateFile = selectedFile;
		        }
			}
		});
		
		Collections.<Component>addAll(evaluateEnablement, directoryText, browseButton);
		
		return browsePanel;
	}
	
	
	private JPanel createButtonPanel() {
		JPanel parentPanel = new JPanel(new BorderLayout());
		parentPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		JButton createButton = new JButton("Create Thematic Map");
		buttonPanel.add(createButton);
		
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateThematicMap();				
			}
		});
		
		parentPanel.add(buttonPanel, BorderLayout.CENTER);
		return parentPanel;
	}
	
	
	
	private List<Integer> getShuffleFlagValues() {
		try {
			String[] allFlags = evaluateText.getText().split(",");
	        List<Integer> allFlagValues = new ArrayList<Integer>(allFlags.length);
	        for(String flag : allFlags) {
	            allFlagValues.add(Integer.parseInt(flag));
	        }
	        return allFlagValues.isEmpty() ? null : allFlagValues;
		} catch(NumberFormatException e) {
			return null;
		}
	}
	
	private boolean validateAndShowErrors() {
		if(evaluateCheck.isSelected() && evaluateFile == null) {
			showError("Please browse for a save file for the shuffle evaluations");
			return false;
		}
		if(statisticsRadio.isSelected() && shuffRadio.isSelected() && evaluateCheck.isSelected()) {
			if(getShuffleFlagValues() == null) {
				showError("Evaluation flags must be integers separated by commas.");
				return false;
			}
			try {
				Integer.parseInt(iterationsText.getText());
			} 
			catch (NumberFormatException e) {
            	showError("Shuffle iterations must be an integer.");
            	return false;
            }  
		}
		return true;
	}
	
	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	
	private void generateThematicMap() {
		if(!validateAndShowErrors()) {
			return;
		}
		try {
			ThematicMapTask task = thematicMapTaskProvider.get();
			
			task.setNetworkName(networkName);
			task.setAttributeName((String)attributeCombo.getSelectedItem());
			String weightAttributeName = (String)weightCombo.getSelectedItem();
	        if(!NONE.equals(weightAttributeName)) {
	        	task.setWeightAttributeName(weightAttributeName);
	        }
			
	        if(hyperRadio.isSelected()) {
	        	task.setStatistic(ThematicMapTask.StatisticType.HYPER);
	        }
	        else if(culmRadio.isSelected()) {
	        	task.setStatistic(ThematicMapTask.StatisticType.HYPER_CULM);
	        }
	        else if(shuffRadio.isSelected()) {
	        	List<Integer> allFlagValues = null;
        		if(evaluateCheck.isSelected()) {
	                allFlagValues = getShuffleFlagValues();
        		}
        		int shuffleIterations = Integer.parseInt(iterationsText.getText());
        		task.setShuffleStatistic(allFlagValues, shuffleIterations, evaluateFile);
	        }
	        
	        task.setSingleNodes(singleNodesCheck.isSelected());
	        task.setRemoveSelfEdges(removeSelfEdges.isSelected());
	        task.setEdgeWidthType(statisticsRadio.isSelected() ?  ThematicMapTask.EdgeWidthType.STATISTICS : ThematicMapTask.EdgeWidthType.COUNT);
	        
	        taskManager.execute(new TaskIterator(task));
		}
		finally {
			dispose();
		}
	}
	
	
	private void updateEnablement() {
		setEnabled(shuffleEnablement, false);
		setEnabled(evaluateEnablement, false);
		
		if(shuffRadio.isSelected()) {
			setEnabled(shuffleEnablement, true);
			if(evaluateCheck.isSelected()) {
				setEnabled(evaluateEnablement, true);
			}
		}
		
		if(noneRadio.isSelected()) {
			countRadio.setSelected(true);
			statisticsRadio.setEnabled(false);
		} else {
			statisticsRadio.setEnabled(true);
		}
	}
	
	private static void setEnabled(Collection<Component> components, boolean enabled) {
		for(Component c : components) {
			c.setEnabled(enabled);
		}
	}
	
	private static void groupButtons(AbstractButton... buttons) {
		ButtonGroup group = new ButtonGroup();
		for(AbstractButton button : buttons) {
			group.add(button);
		}
		if(buttons.length > 0)
			buttons[0].setSelected(true);
	}
	
	private Collection<String> getAttributes(boolean node) {
        CyNetwork network = applicationManager.getCurrentNetwork();
        CyTable table = node ? network.getDefaultNodeTable() : network.getDefaultEdgeTable();
		Collection<CyColumn> columns = table.getColumns();
        
    	Set<String> attributes = new TreeSet<String>(new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});
    	
    	for(CyColumn column : columns) {
    		if(!CyIdentifiable.SUID.equals(column.getName()) && (node || Number.class.isAssignableFrom(column.getType()))) {
    			attributes.add(column.getName());
    		}
    	}
    	return attributes;
    }
	
}

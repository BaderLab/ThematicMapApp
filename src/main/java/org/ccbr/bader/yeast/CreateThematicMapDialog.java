package org.ccbr.bader.yeast;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;
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
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
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
import org.cytoscape.view.model.CyNetworkViewManager;

import com.google.inject.Inject;
import com.google.inject.Provider;


@SuppressWarnings("serial")
public class CreateThematicMapDialog extends JDialog {

	private final String NONE = "[None]";
	
	
	private final CyApplicationManager applicationManager;
	
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private Provider<ThematicMap> thematicMapProvider;
	
	
	@Inject
	public CreateThematicMapDialog(CySwingApplication application, CyApplicationManager applicationManager) {
		super(application.getJFrame(), true);
		setTitle("Create Thematic Map");
		
		this.applicationManager = applicationManager;
		createContents();
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
		
		JComboBox<String> attributeCombo = new JComboBox<String>();
		for(String attribute : getAttributes(false)) {
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
		
		JComboBox<String> weightCombo = new JComboBox<String>();
		weightCombo.addItem(NONE);
		for(String attribute : getAttributes(true)) {
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
		
		JRadioButton countRadio = new JRadioButton("Count");
		JRadioButton statisticsRadio = new JRadioButton("Statistics");
		groupButtons(countRadio, statisticsRadio);
		
		attributePanel.add(countRadio);
		attributePanel.add(statisticsRadio);
		
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
		
		JRadioButton noneRadio  = new JRadioButton("None");
		JRadioButton hyperRadio = new JRadioButton("Hypergeometric Probability");
		JRadioButton culmRadio  = new JRadioButton("Hypergeometric Probability (Cumulative)");
		JRadioButton shuffRadio = new JRadioButton("Shuffle"); 
		groupButtons(noneRadio, hyperRadio, culmRadio, shuffRadio);
		
		radioPanel.add(noneRadio);
		radioPanel.add(hyperRadio);
		radioPanel.add(culmRadio);
		radioPanel.add(shuffRadio);
			
		statisticsPanel.add(radioPanel, BorderLayout.NORTH);
		
		
		JPanel shufflePanel = new JPanel();
		shufflePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		GridBagConstraints gbc;
		shufflePanel.setLayout(new GridBagLayout());
		shufflePanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		
		JPanel iterationsPanel = new JPanel();
		iterationsPanel.setLayout(new BoxLayout(iterationsPanel, BoxLayout.X_AXIS));
		iterationsPanel.add(new JLabel("Iterations:"));
		JTextField iterationsText = new JFormattedTextField("1000");
		iterationsPanel.add(iterationsText);
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		shufflePanel.add(iterationsPanel, gbc);
		
		JCheckBox evaluateCheck = new JCheckBox("Evaluate shuffle iterations");
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		shufflePanel.add(evaluateCheck, gbc);

		JTextField evaluateText = new JTextField();
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
		
		JPanel browsePanel = new JPanel(new BorderLayout());
		JTextField directoryText = new JTextField();
		browsePanel.add(directoryText, BorderLayout.CENTER);
		JButton browseButton = new JButton("Browse...");
		browsePanel.add(browseButton, BorderLayout.EAST);
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weighty = 1.0;
		shufflePanel.add(browsePanel, gbc);
		
		statisticsPanel.add(shufflePanel, BorderLayout.CENTER);
		return statisticsPanel;
	}
	
	
	private JPanel createButtonPanel() {
		JPanel parentPanel = new JPanel(new BorderLayout());
		parentPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		JButton createButton = new JButton("Create Thematic Map");
		buttonPanel.add(createButton);
		
		parentPanel.add(buttonPanel, BorderLayout.CENTER);
		return parentPanel;
	}
	
	
	private static void groupButtons(AbstractButton ... buttons) {
		ButtonGroup group = new ButtonGroup();
		for(AbstractButton button : buttons) {
			group.add(button);
		}
		buttons[0].setSelected(true);
	}
	
	private Collection<String> getAttributes(boolean numericOnly) {
        CyNetwork network = applicationManager.getCurrentNetwork();
        Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns();
        
    	Set<String> attributes = new TreeSet<String>();
    	for(CyColumn column : columns) {
    		if(!CyIdentifiable.SUID.equals(column.getName()) && (!numericOnly || Number.class.isAssignableFrom(column.getType()))) {
    			attributes.add(column.getName());
    		}
    	}
    	return attributes;
    }
	
	
}

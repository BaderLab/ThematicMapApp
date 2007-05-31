 package org.ccbr.bader.yeast;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import cytoscape.visual.VisualMappingManager;

public class ThematicMapPanelAction implements ActionListener {

	/*Notes whether or not the GOSlimPanel has been opened yet or now */
	boolean alreadyOpened = false;
//	GOSlimPanel goSlimPanel =null;

	protected String directlyAnnotatedGenesAttributeName = "GENE_ASSOC.DIRECTLY_ANNOTATED_GENES";
	protected String inferredAnnotatedGenesAttributeName = "GENE_ASSOC.INFERRED_ANNOTATED_GENES";
	

	
	public ThematicMapPanelAction() {
		// TODO Auto-generated constructor stub
	}


	private static final String lsep = System.getProperty("line.separator");
	
	public void actionPerformed(ActionEvent event) {
		//ThematicMapFunctionPrototype.createThematicMap("canonicalName");
		ThematicMapFunctionPrototype.createThematicMap("annotation.GO BIOLOGICAL_PROCESS");
//		display ThematicMap Main Panel in left cytopanel
		CytoscapeDesktop desktop = Cytoscape.getDesktop();
		CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);
		
		if (event.getSource() instanceof JMenuItem) {
			JMenuItem src = (JMenuItem) event.getSource();
			if (src.getText().equals("Exit ThemeMapper")) {
				cytoPanel.remove(goSlimmerSessionsTabbedPane);
				goSlimmerSessionsTabbedPane = null;
				//delete goslimmer specific attributes:
//				ThematicMapUtil.deleteThematicMapAttributes();
				alreadyOpened=false;
			}
			else if (src.getText().equals("Start ThemeMapper")) {
				
				//Unfortuntately, because cyattributes are defined globally for all nodes with the same id, we can't at this time 
				//use goslimmer on two dags at the same time
				
				if (alreadyOpened) {
					JOptionPane.showMessageDialog(desktop, "ThematicMap cannot be used to edit more than one GO Tree at a time."
							+ lsep + "To edit a new graph, select to close ThematicMap from Pluggins->ThematicMap and then start "
							+ lsep + "on the new GO Tree you wish to edit.");
					return;
				}
				


				

//				if (!alreadyOpened) {
//					//initialize the goSlimPanel and add it to the cytopanel
//					goSlimPanel = new GOSlimPanel();
//					cytoPanel.add(goSlimPanel);
//					alreadyOpened = true;
//				}
				VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
		        if (!vmm.getCalculatorCatalog().getVisualStyleNames().contains("GOSLIMMERVS")) {
		            vmm.getCalculatorCatalog().addVisualStyle(new ThemeMapVisualStyle(vmm.getVisualStyle(),"GOSLIMMERVS"));
		        }
				//vmm.setVisualStyle("GOSLIMMERVS");
				
				//create a new goslimmersession and add it to the gomainpanel tabbed panel
				//note that we probably can't manipulate multiple go graphs at the same time due to the way attributes are saved
				
				CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
				
				//ensure that the network is not null
				if (currentNetwork==null) {
					JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Cannot start ThematicMap without a GO Tree Network.  Please load a GO Ontology Tree, selected it as the current network, and then start ThematicMap","Error - cannot start ThematicMap",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				
				
//				ThematicMapSession newSession =  new ThematicMapSession(currentNetwork);
//				GOSlimPanel newSessionPanel = new GOSlimPanel(newSession.getNamespaceToController(),newSession);
				

				if (!alreadyOpened) {
//					goSlimmerSessionsTabbedPane = new JTabbedPane();
//					goSlimmerSessionsTabbedPane = newSessionPanel;
					
					JPanel layoutPanel = new JPanel();
//					layoutPanel.setLayout(new CardLayout());
					
					goSlimmerSessionsTabbedPane = layoutPanel;
//					goSlimmerSessionsTabbedPane.add(newSessionPanel);
					goSlimmerSessionsTabbedPane.add(new JPanel());
//					goSlimmerSessionsTabbedPane.add(newSessionPanel,BorderLayout.PAGE_START);
//					goSlimmerSessionsTabbedPane.add(new JPanel(),BorderLayout.PAGE_END);
					
					
					cytoPanel.add("ThematicMap",goSlimmerSessionsTabbedPane);
					alreadyOpened = true;
				}
				
				//add the new session pane to the goslimmer cytopanel
//				goSlimmerSessionsTabbedPane.add(currentNetwork.getTitle(),newSessionPanel);
				
				//get the index of the panel and tell it to dock it
				int index = cytoPanel.indexOfComponent(goSlimmerSessionsTabbedPane);
				cytoPanel.setSelectedIndex(index);
				cytoPanel.setState(CytoPanelState.DOCK);
			}
		}
		


	}
	private JPanel goSlimmerSessionsTabbedPane;


	
}

package org.ccbr.bader.yeast;


import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;


/**
 * User: mikematan
 * Date: Feb 12, 2007
 * Time: 6:03:32 PM
 */
public class ThematicMapPlugin extends CytoscapePlugin 
{
    public ThematicMapPlugin() {
        
        //set-up menu options in plugins menu
        JMenu menu = Cytoscape.getDesktop().getCyMenus().getOperationsMenu();
        JMenuItem item;

        //MCODE submenu
        JMenu submenu = new JMenu("ThematicMap");

        ThematicMapPanelAction actionListener = new ThematicMapPanelAction();
        
        //MCODE panel
        //add the start goslimmer option
        item = new JMenuItem("Start ThematicMap");
        item.addActionListener(actionListener);
        //submenu.add(item);
        //add the exit goslimmer option
        item = new JMenuItem("Exit ThematicMap");
        item.addActionListener(actionListener);
        //submenu.add(item);
        submenu.add(new ThematicMapMenu());
        menu.add(submenu);
    }

	@Override
	public void activate() {
		// TODO Auto-generated method stub
		super.activate();
		//cytoscape.cruft.obo.BiologicalProcessAnnotationReader meh = new cytoscape.cruft.obo.BiologicalProcessAnnotationReader(null, meh);
		
	}
}

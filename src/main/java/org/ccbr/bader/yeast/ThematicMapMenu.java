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
 * * Description: Cytomenu submenu for calling thematic map functionality
 */
package org.ccbr.bader.yeast;

import java.awt.HeadlessException;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import java.io.IOException;

//import csplugins.layout.algorithms.graphPartition.AbstractLayout;
import csplugins.layout.algorithms.graphPartition.AttributeCircleLayout;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

public class ThematicMapMenu extends JMenu implements MenuListener {

	public ThematicMapMenu() {
	    // call the super constructor
	    super("Create Thematic Map Using Attribute");

	    // add this class as a listener. See method menuSelected()
	    this.addMenuListener(this);
	}
	

	public void menuCanceled(MenuEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void menuDeselected(MenuEvent arg0) {
		// TODO Auto-generated method stub

	}

	  /**
	    * Creates a new menu item that has the title "No Attributes Loaded."
	    * The item is disabled. This method is called by menuSelected() when the
	    * node attributes CyNetwork could not be loaded.
	    */
	  private JMenuItem getNewNoAttributesMenuItem()
	  {
	    JMenuItem item = new JMenuItem("No Attributes Loaded");
	    item.setEnabled(false);
	    return item;
	  }
	
	  //TODO modify this to call the thematic map creation task instead
	  /**
	    * Creates a new menu item that has the title of _attributeName.
	    * When this menu item is selected, it will perform the
	    * AttributeCircleLayout algorithm based on the attribute specified.
	    * This method is called for each attribute in the current CyNetwork
	    * by menuSelected.
	    */
	  private JMenuItem getNewAttributesMenuItem(final String _attributeName)
	  {
	    return new JMenuItem(
	      new AbstractAction(_attributeName)
	      {
	        // this method is called whenever this menu item is selected
	        public void actionPerformed(ActionEvent _e)
	        {
	          // run this in a different thread

	          SwingUtilities.invokeLater(
	            new Runnable()
	            {
	              public void run()
	              {
                      // get the network and attributes
                      CyNetwork network = Cytoscape.getCurrentNetwork();
                      CyAttributes attributes = Cytoscape.getNodeAttributes();

//	                // create a new AttributeCircleLayout object, based on
//	                // which menu item was selected
//	                AbstractLayout layoutObj = new AttributeCircleLayout(network,
//	                                                 attributes, _attributeName);
//
//	                // layout the network
//	                layoutObj.layout();

                      ThematicMapDialog tmd;

                      try {
                          tmd = new ThematicMapDialog(Cytoscape.getDesktop(), true);
                          tmd.setLocationRelativeTo(Cytoscape.getDesktop());
                          tmd.setVisible(true);
                      }
                      catch (IOException e1) {
                          e1.printStackTrace();
                      }

                      CyNetwork inputNetwork = Cytoscape.getCurrentNetwork();
                      String themeAttributeName = _attributeName;
                      CyNetwork thematicMap = ThematicMapFunctionPrototype.createThematicMap(inputNetwork, themeAttributeName);
                      ThematicMapFunctionPrototype.createThematicMapDefaultView(thematicMap, themeAttributeName, ThematicMapFunctionPrototype.EDGE_WIDTH_COUNT);

                  } // end run()

	            } // end new Runnable

	          ); // end invokeLater()

	        } // end actionPerformed()

	      } // end new AbstractAction

	    ); // end return new JMenuItem

	  } // end getNewAttributesMenuItem()
	  
	  /**
	    * This is the heart of the class. Whenever this menu is selected,
	    * menuSelected() is called. This method builds a submenu of a list of
	    * attributes.
	    */
	  public void menuSelected(MenuEvent _e)
	  {
	    // remove all the previosly created menu items
	    this.removeAll();

	    // get the attributes
	    CyAttributes attributes = Cytoscape.getNodeAttributes();

	    // if there aren't any attributes, call getNewNoAttributesMenuItem()
	    if (attributes == null)
	    {
	      this.add(getNewNoAttributesMenuItem());
	      return;
	    }

	    // get the list of attributes in a String array. If the array
	    // is null or empty, call getNewNoAttributesMenuItem()
	    String attributeNames[] = attributes.getAttributeNames();
	    if (attributeNames == null || attributeNames.length == 0)
	    {
	      this.add(getNewNoAttributesMenuItem());
	      return;
	    }

	    // iterate over the attributeNames[] array, calling
	    // getNewAttributesMenuItem() for each attribute

	    for (int i = 0; i < attributeNames.length; i++)
	    {
	      this.add(getNewAttributesMenuItem(attributeNames[i]));
	    }

	  } // end menuSelected()
	
}

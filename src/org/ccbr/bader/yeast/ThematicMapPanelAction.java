package org.ccbr.bader.yeast;

/**
 * Created by IntelliJ IDEA.
 * User: lmorrison
 * Date: Apr 29, 2008
 * Time: 4:06:23 PM
 * To change this template use File | Settings | File Templates.
 */

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import cytoscape.Cytoscape;

import java.io.IOException;

public class ThematicMapPanelAction implements ActionListener {

    public ThematicMapPanelAction() {
    }

    public void actionPerformed(ActionEvent event) {

        ThematicMapDialog tmd;

        try {
            tmd = new ThematicMapDialog(Cytoscape.getDesktop(), true);
            tmd.setLocationRelativeTo(Cytoscape.getDesktop());
            tmd.setVisible(true);
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }

    }
}

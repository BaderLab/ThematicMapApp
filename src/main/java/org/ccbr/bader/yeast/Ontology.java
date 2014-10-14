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
 * * Description: Class for encapsulating information about generic ontologies
 */
package org.ccbr.bader.yeast;

import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;


/**This is a class which will be used for representing generic ontologies.  It may provide easy mechanisms for integrating with the cytoscape 
 * CyNetwork representation of graphs.  The particular graph structure is works with are directed acyclic graphs (DAGs), an example of which are 
 * the Gene Ontology (GO) term graphs.
 * 
 * 
 * @author mikematan
 *
 */
public interface Ontology {
	
	
	/**Sets the name/edge type to use when creating ISA edges
	 * @param isa the isa type name
	 */
	public void setISAEdgeTypeName(String isa);
	public String getISAEdgeTypeName();
	
	public Set<CyNode> getChildren(CyNode node);
	
	public Set<CyNode> getParents(CyNode node);
	
	/**Adds node to the graph as child of parent (ISA relationship)
	 * @param parent
	 * @param child
	 */
	public void addChild(CyNode parent, CyNode child);
	
	/**Creates an ISA edge between two existing nodes 
	 * @param parent existing parent node for new connection
	 * @param child existing child node for new connection
	 * @return newly created edge between parent and child
	 */
	public CyEdge createISAEdge(CyNode parent, CyNode child);
	
	public void removeEdge(CyEdge edge);
	
}

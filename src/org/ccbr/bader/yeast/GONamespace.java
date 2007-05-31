/**
 * 
 */
package org.ccbr.bader.yeast;

public enum GONamespace{
	MolFun("Molecular Function","GO:0003674","F"),
	BioPro("Biological Process","GO:0008150","P"),
	CelCom("Cellular Component","GO:0005575","C");
	
	private String name;
	private String rootTermId;
	private String geneAnnotationAbreviation;
	
	GONamespace(String name,String rootTermId,String geneAnnotationAbbreviation) {
		this.name = name;
		this.rootTermId = rootTermId;
		this.geneAnnotationAbreviation = geneAnnotationAbbreviation;
	}

	/**
	 * @return the abbreviation for this namespace used in gene annotation files
	 */
	public String getGeneAnnotationAbreviation() {
		return geneAnnotationAbreviation;
	}

	public String getName() {
		return name;
	}

	public String getRootTermId() {
		return rootTermId;
	}

}
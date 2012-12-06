// (C) 2010 Ralf Laemmel

package data.tree;

public class StringTree {

	private String info = null;
	private StringTree[] subtrees = null;
	
	public StringTree(String info, StringTree[] subtrees) {
		this.info = info;
		this.subtrees = subtrees;
	}
	
	public String getInfo() { return info; }
	
	public StringTree[] getSubtrees() { return subtrees; }
}

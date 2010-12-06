// (C) 2010 Ralf Laemmel

package data.tree;

public class StringTree {

	private String info = null;
	private StringTree[] subtrees = null;
	
	public StringTree(String info, StringTree[] subtrees) {
		this.info = info;
		this.subtrees = subtrees;
	}
		
	public void print() {
		print(0);
	}

	public void print(int indent) {
		System.out.print('|');
		for (int i=0; i<3*indent; i++) System.out.print('-'); 
		System.out.print("- ");
		System.out.println(info);
		indent++;
		for (int i=0; i<subtrees.length; i++)
			subtrees[i].print(indent);
	}	
	
	public static void main(String[] args) {
		StringTree t1 =
			new StringTree("Java types", new StringTree[] {
			  new StringTree("Primitive types", new StringTree[] {
			    new StringTree("boolean", new StringTree[] {}),
			    new StringTree("char", new StringTree[] {}),
			    new StringTree("short", new StringTree[] {}),
			    new StringTree("int", new StringTree[] {}),
			    new StringTree("long", new StringTree[] {}),
			    new StringTree("float", new StringTree[] {}),
			    new StringTree("double", new StringTree[] {})}),
		      new StringTree("Reference types", new StringTree[] {
			    new StringTree("String", new StringTree[] {}),
			    new StringTree("Array types", new StringTree[] {
		          new StringTree("int[]", new StringTree[] {}),
			      new StringTree("...", new StringTree[] {})}),
			    new StringTree("Wrapper types", new StringTree[] {
			      new StringTree("Integer", new StringTree[] {}),
				  new StringTree("...", new StringTree[] {})})})});
		t1.print();
	}

}

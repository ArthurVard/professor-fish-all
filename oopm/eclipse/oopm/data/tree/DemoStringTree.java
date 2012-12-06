package data.tree;

public class DemoStringTree {

	public static void print(StringTree t) {
		print(t, 0);
	}

	public static void print(StringTree t, int indent) {
		System.out.print('|');
		for (int i=0; i<3*indent; i++) System.out.print('-'); 
		System.out.print("- ");
		System.out.println(t.getInfo());
		indent++;
		for (int i=0; i<t.getSubtrees().length; i++)
			print(t.getSubtrees()[i],indent);
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
		print(t1);
	}	
}

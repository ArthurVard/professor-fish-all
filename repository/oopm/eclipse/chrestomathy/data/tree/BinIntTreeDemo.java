// (C) 2009-10 Ralf Laemmel

package data.tree;

import java.util.Arrays;

public class BinIntTreeDemo {

	public static void print(BinIntTree t) {
		print(t, 0);
	}

	public static void print(BinIntTree t, int indent) {
		System.out.print('|');
		for (int i=0; i<3*indent; i++) System.out.print('-'); 
		System.out.print("- ");
		System.out.println(t.getInfo());
		indent++;
		if (t.getLeft()!=null) print(t.getLeft(), indent);
		if (t.getRight()!=null) print(t.getRight(), indent);
	}	
			
	public static int sum(BinIntTree t) {
		return 	  t.getInfo()
				+ (t.getLeft() != null ? sum(t.getLeft()) : 0)
				+ (t.getRight() != null ? sum(t.getRight()) : 0);
	}
	
	public static void main(String[] args) {
		BinIntTree t1 = 
			new BinIntTree(4,
				new BinIntTree(2,
					new BinIntTree(1,null,null),
					new BinIntTree(3,null,null)),
				new BinIntTree(5,
					null,
					null));
		print(t1);
		System.out.println(t1.find(0));
		System.out.println(t1.find(1));
		System.out.println(t1.find(2));
		System.out.println(t1.find(3));
		System.out.println(t1.find(4));
		System.out.println(t1.find(5));
		System.out.println(t1.find(6));
		System.out.println(sum(t1));
		System.out.println(Arrays.toString(t1.inorder()));		
	}
}

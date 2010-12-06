// (C) 2009-10 Ralf Laemmel

package data.tree;

public class BinIntTree {

	private int info;
	private BinIntTree left, right;
	
	public BinIntTree(int info, BinIntTree left, BinIntTree right) {
		this.info = info;
		this.left = left;
		this.right = right;
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
		if (left!=null) left.print(indent);
		if (right!=null) right.print(indent);
	}	
		
	public boolean find(int x) {
		return
			(x < info) ?
			 	  left!=null && left.find(x)
				: (x > info) ?
				 	  right!=null && right.find(x)
					: (x==info);
	}
	
	public int sum() {
		return 	  this.info
				+ (this.left != null ? this.left.sum() : 0)
				+ (this.right != null ? this.right.sum() : 0);
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
		t1.print();
		System.out.println(t1.find(0));
		System.out.println(t1.find(1));
		System.out.println(t1.find(2));
		System.out.println(t1.find(3));
		System.out.println(t1.find(4));
		System.out.println(t1.find(5));
		System.out.println(t1.find(6));
		System.out.println(t1.sum());
	}
}

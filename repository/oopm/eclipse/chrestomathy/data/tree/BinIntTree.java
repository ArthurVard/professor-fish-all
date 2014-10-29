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
	
	public int getInfo() { return info; }
	public BinIntTree getLeft() { return left; }
	public BinIntTree getRight() { return right; }
		
	public boolean find(int x) {
		return
			(x < info) ?
			 	  left!=null && left.find(x)
				: (x > info) ?
				 	  right!=null && right.find(x)
					: true;
	}
	
	/** @return the number of nodes in the tree */
	public int nodes() {
		return 1 
				+ (left==null ? 0 : left.nodes())
				+ (right==null ? 0 : right.nodes());
	}
	
	/** @return inorder serialization of the tree */
	public int[] inorder() {
		int[] a = new int[nodes()];
		inorder(a,0);
		return a;
	}
	
	private int inorder(int[] a, int i) {
		i = left==null ? i : left.inorder(a, i);
		a[i++] = info;
		i = right==null ? i : right.inorder(a, i);
		return i;
	}
}
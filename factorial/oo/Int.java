// (C) 2010, Ralf Laemmel

package oo;

public class Int {
	private int n;
	public Int(int n) { this.n = n; }
	public String toString() { return n + ""; }
	public boolean isZero() { return (n==0); }
	public Int pred() { return new Int(n-1); }	
	public Int succ() { return new Int(n+1); }	
}

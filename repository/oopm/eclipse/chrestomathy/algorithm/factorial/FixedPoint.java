//
// Careful: this is not quite basic stuff.
// Source:
// http://www.arcfn.com/2009/03/y-combinator-in-arc-and-java.html
//

package algorithm.factorial;

public class FixedPoint {
	
	interface IntFunc { int apply(int n); }
	interface IntFuncToIntFunc { IntFunc apply(IntFunc f); };
	interface FuncToIntFunc { IntFunc apply(FuncToIntFunc x); }
	interface IntFuncToIntFuncToIntFunc { IntFunc apply(IntFuncToIntFunc r);};
	
	public static void main(String args[]) {
		System.out.println(
			// Y combinator
			(new IntFuncToIntFuncToIntFunc() {
				public IntFunc apply(final IntFuncToIntFunc r) {
					return (new FuncToIntFunc() {
						public IntFunc apply(final FuncToIntFunc f) {
							return f.apply(f); }})
							.apply(
								new FuncToIntFunc() {
									public IntFunc apply(final FuncToIntFunc f) {
										return r.apply(
												new IntFunc() { public int apply(int x) {
													return f.apply(f).apply(x); }});}});}}
		    ).apply(
		        // Recursive function generator
		    	new IntFuncToIntFunc() { public IntFunc apply(final IntFunc f) {
		    		return new IntFunc() { public int apply(int n) {
		    			if (n == 0) return 1; else return n * f.apply(n-1); }};}} 

		    ).apply(5));
		  }
}

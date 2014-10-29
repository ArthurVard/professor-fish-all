// (C) 2009 Ralf Laemmel

package algorithm.hanoi;

import java.util.Arrays;

/**
 * Iterator-based approach to the towers of Hanoi
 */
public class ArrayBased {

	/*
	 *  Solves the Towers of Hanoi problem on N discs. The discs are labeled
     *  in increasing order of size from 1 to N and the poles are labeled
     *  A, B, and C. Here is a solution sequence for 3 discs:
     *
     * [ 	Move(1,A,C),
     *  	Move(2,A,B),
     *  	Move(1,C,B),
     *  	Move(3,A,C),
     *  	Move(1,B,A),
     *  	Move(2,B,C),
     *  	Move(1,A,C)		]
     *
	 */
	public static void move(Move[] r, int n, String from, String temp, String to) {
		move(r, 0, n, from, temp, to);
	}

	private static int move(Move[] r, int p, int n, String from, String temp, String to) {
		if (n == 0)
			return p;
		p = move(r, p, n - 1, from, to, temp);
		Move m = new Move(n, from, to);
		r[p++] = m;
		return move(r, p, n - 1, temp, from, to);
	}

	public static void main(String[] args) {
		Move[] r = new Move[7];
		move(r, 3, "A", "B", "C");
		System.out.println(Arrays.toString(r));
	}

}

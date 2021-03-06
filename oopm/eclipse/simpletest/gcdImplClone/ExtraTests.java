import static org.junit.Assert.*;

/**
 * @author Ralf Lämmel
 * Extra tests for GCD algorithm
 */
public class ExtraTests {
	public static void main(String[] args) {
		assertEquals(4, Functionality.gcd(8,12)); // swap arguments
		assertEquals(25, Functionality.gcd(175,25)); // bigger numbers
		assertEquals(64, Functionality.gcd(1024,192)); // much bigger numbers
	}
}
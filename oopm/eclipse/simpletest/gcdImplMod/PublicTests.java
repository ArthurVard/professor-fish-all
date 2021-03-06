import static org.junit.Assert.*;

/**
 * @author Ralf Lämmel
 * Public tests for GCD algorithm
 */
public class PublicTests {
	public static void main(String[] args) {		
		assertEquals(1, Functionality.gcd(6,5));
		assertEquals(2, Functionality.gcd(6,4));
		assertEquals(3, Functionality.gcd(9,6));
		assertEquals(4, Functionality.gcd(12,8));
	}
}
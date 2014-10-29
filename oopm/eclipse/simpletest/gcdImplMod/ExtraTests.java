import static org.junit.Assert.*;

/**
 * @author Ralf Lämmel
 * Extra tests for GCD algorithm
 */
public class ExtraTests {
	public static void main(String[] args) {		
		assertEquals(25, Functionality.gcd(175,25));
		assertEquals(64, Functionality.gcd(1024,192));
	}
}
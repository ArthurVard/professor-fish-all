// (C) 2009-2014 Ralf Laemmel

package data.complex.encapsulation;

/**
 * The data type of complex numbers.
 * The representation uses the Cartesian form.
 */
public class Complex {

	// Store real part
	private double re;

	// Store imaginary part
	private double im;

	/**
	 * Construct a complex number from real and imaginary parts
	 */
	public Complex(double re, double im) {
		this.re = re;
		this.im = im;
	}

	/**
	 * Construct a complex number from polar form arguments
	 */
	public static Complex fromPolar(double magnitude, double angle) {
		double re = magnitude * Math.cos(angle);
		double im = magnitude * Math.sin(angle);
		return new Complex(re, im);
	}

	/**
	 * Getter for real part
	 */
	public double getRe() {
		return re;
	}

	/**
	 * Getter for imaginary part
	 */
	public double getIm() {
		return im;
	}

	/**
	 * Getter for norm of polar form
	 */
	public double getModulus() {
		return Math.hypot(re, im);
	}

	/**
	 * Getter for (radian) angle of polar form
	 */
	public double getRadianAngle() {
		return Math.atan2(im, re);
	}

	/**
	 * Getter for angle of polar form
	 */
	public double getAngle() {
		return getRadianAngle() * 180 / Math.PI;
	}

	public Complex addTo(Complex c) {
		return new Complex(this.getRe() + c.getRe(), this.getIm()
				+ c.getIm());
	}

	public String toString() {
		String result = "Complex(";
		result += Double.toString(getRe());
		result += ",";
		result += Double.toString(getIm());
		result += ")";
		return result;
	}

	public static void main(String[] args) {
		Complex a = new Complex(1, 2);
		System.out.println(a); // prints Complex(1,2)
		Complex b = new Complex(2, 2);
		System.out.println(b); // prints Complex(2,2)
		Complex c = a.addTo(b);
		System.out.println(c); // prints Complex(4,4)
	}

}

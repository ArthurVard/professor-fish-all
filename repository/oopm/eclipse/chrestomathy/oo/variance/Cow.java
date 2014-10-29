package oo.variance;

public class Cow extends Herbivore {
	// This may look like a co-variant refinement.
	// In fact, we overload eat.
	public void eat(Grass food) { }
}

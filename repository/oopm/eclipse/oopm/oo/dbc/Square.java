package oo.dbc;

public class Square extends Rectangle {
	
	protected boolean invariant() {
		return super.invariant() && getWidth() == getHeight();
	}

	public Square(int length) {
		super(length, length);
	}
	
	public int getLength() {
		return getWidth(); 
	}

	public void setLength(int length) {
		setWidth(length); 
	}
	
	public void setHeight(int length) { 		
		setWidth(length);
	}		
	
	public void setWidth(int length) { 		
		assert length > 0; // Precondition
		this.width = length; 
		this.height = length; 
		assert invariant();
	}
}

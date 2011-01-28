package oo.dbc;

public class Rectangle {

	protected int width;
	protected int height;
	
	protected boolean invariant() {
		return getWidth() > 0 && getHeight() > 0;
	}

	public Rectangle(int width, int heigth) {
		assert width > 0 && height > 0; // Precondition
		this.width = width;
		this.height = heigth;
		assert invariant();
	}
	
	public int getWidth() {
		return width; 
	}
	
	public int getHeight() { 
		return height; 
	}

	public void setWidth(int width) { 		
		assert width > 0; // Precondition
		this.width = width; 
		assert invariant();
	}
	
	public void setHeight(int height) { 
		assert height > 0; // Precondition
		this.height = height; 
		assert invariant();
	}

	public int getPerimeter() {
		return (width+height)*2;
	}
}

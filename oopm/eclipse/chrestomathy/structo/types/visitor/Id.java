// (C) 2009 Ralf Laemmel

package structo.types.visitor;

public class Id extends Expression {
	private String value;
	public Id(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	public void accept(Visitor v) {
		v.visit(this);
	}
}

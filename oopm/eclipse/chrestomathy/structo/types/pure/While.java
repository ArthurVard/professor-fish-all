// (C) 2009 Ralf Laemmel

package structo.types.pure;

public class While extends Statement {
	private Expression cond;
	private Statement body;
	public While(Expression cond, Statement body) {
		this.cond = cond;
		this.body = body;
	}
	public Expression getCond() {
		return cond;
	}
	public Statement getBody() {
		return body;
	}
}

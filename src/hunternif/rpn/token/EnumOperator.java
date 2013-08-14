package hunternif.rpn.token;

enum EnumOperator {
	ADD("+", 0), SUBTRACT("-", 0),
	MULTIPLY("*", 1), DIVIDE("/", 1),
	POWER("^", 2);
	
	public String notation;
	public int precedence;
	
	private EnumOperator(String notation, int precendence) {
		this.notation = notation;
		this.precedence = precendence;
	}
}

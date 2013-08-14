package hunternif.rpn.token;

import hunternif.rpn.CalculationException;

public abstract class TokenComputable extends Token {
	public int precedence;
	public int args;
	
	public TokenComputable(String notation, int args, int precedence) {
		super (notation);
		this.args = args;
		this.precedence = precedence;
	}
	
	public abstract double compute(double[] params) throws CalculationException;
	
	public abstract boolean isInfix();
}

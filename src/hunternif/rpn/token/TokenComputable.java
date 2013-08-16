package hunternif.rpn.token;

import hunternif.rpn.CalculationException;

public abstract class TokenComputable extends Token {
	public int args;
	
	public TokenComputable(String notation, int args, int precedence) {
		super(notation, precedence);
		this.args = args;
	}
	
	public abstract double compute(double[] params) throws CalculationException;
	
	public abstract boolean isInfix();
}

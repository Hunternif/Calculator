package hunternif.rpn.token;

import java.util.ArrayList;
import java.util.List;

/** Binary operator. */
public final class TokenOperator extends TokenComputable {
	
	private static enum EnumOperator {
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
	
	public static final List<TokenOperator> operators = new ArrayList<>();
	static {
		for (EnumOperator type : EnumOperator.values()) {
			operators.add(new TokenOperator(type));
		}
	}
	
	private EnumOperator type;
	
	private TokenOperator(EnumOperator type) {
		super(type.notation, 2, type.precedence);
		this.type = type;
	}
	
	@Override
	public boolean isInfix() {
		return true;
	}
	
	@Override
	public double compute(double[] params) {
		double a = params[0];
		double b = params[1];
		switch (type) {
		case ADD:
			return a + b;
		case SUBTRACT:
			return a - b;
		case MULTIPLY:
			return a * b;
		case DIVIDE:
			return a / b;
		case POWER:
			return Math.pow(a, b);
		}
		return a;
	}
	
	@Override
	public String toString() {
		return notation;
	}
}

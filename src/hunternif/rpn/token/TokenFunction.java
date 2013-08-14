package hunternif.rpn.token;

import hunternif.rpn.CalculationException;

import java.util.ArrayList;
import java.util.List;

public class TokenFunction extends TokenComputable {
	
	private static enum EnumFunction {
		SIN("sin", 1), COS("cos", 1), TG("tg", 1), CTG("ctg", 1),
		ARCSIN("arcsin", 1), ARCCOS("arccos", 1), ARCTG("arctg", 1), ARCCTG("arcctg", 1),
		SH("sh", 1), CSH("csh", 1),
		SQRT("sqrt", 1), EXP("exp", 1), SIGN("sign", 1), ABS("abs", 1);
		public String notation;
		public int args;
		private EnumFunction(String notation, int args) {
			this.notation = notation;
			this.args = args;
		}
	}
	
	public static final List<TokenFunction> functions = new ArrayList<>();
	static {
		for (EnumFunction type : EnumFunction.values()) {
			functions.add(new TokenFunction(type));
		}
	}
	
	private EnumFunction type;
	private TokenFunction(EnumFunction type) {
		this(type.notation, type.args);
		this.type = type;
	}
	public TokenFunction(String notation, int args) {
		super(notation, args, Integer.MAX_VALUE);
	}
	
	@Override
	public boolean isInfix() {
		return false;
	}
	
	@Override
	public String toString() {
		return notation;
	}
	@Override
	public double compute(double[] params) throws CalculationException {
		if (type != null) {
			double a = params[0];
			switch (type) {
			case SIN:
				return Math.sin(a);
			case COS:
				return Math.cos(a);
			case TG:
				return Math.tan(a);
			case CTG:
				return 1.0 / Math.tan(a);
			case ARCSIN:
				return Math.asin(a);
			case ARCCOS:
				return Math.acos(a);
			case ARCTG:
				return Math.atan(a);
			case ARCCTG:
				return Math.PI * 0.5 - Math.atan(a);
			case SH:
				return Math.sinh(a);
			case CSH:
				return Math.cosh(a);
			case SQRT:
				return Math.sqrt(a);
			case EXP:
				return Math.exp(a);
			case SIGN:
				return Math.signum(a);
			case ABS:
				return Math.abs(a);
			}
			return a;
		} else {
			throw new CalculationException("Unimplemented function: " + notation);
		}
	}
}

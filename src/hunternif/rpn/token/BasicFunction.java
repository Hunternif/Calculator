package hunternif.rpn.token;

import hunternif.rpn.CalculationException;

import java.util.ArrayList;
import java.util.List;

public class BasicFunction extends TokenFunction {
	
	private static enum EnumFunction {
		SIN("sin", 1), COS("cos", 1), TG("tg", 1), CTG("ctg", 1),
		ARCSIN("arcsin", 1), ARCCOS("arccos", 1), ARCTG("arctg", 1), ARCCTG("arcctg", 1),
		SH("sh", 1), CSH("csh", 1),
		SQRT("sqrt", 1), EXP("exp", 1), LN("ln", 1), SIGN("sign", 1), ABS("abs", 1),
		MIN("min", 2), MAX("max", 2), LOG("log", 2);
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
			functions.add(new BasicFunction(type));
		}
	}
	
	private EnumFunction type;
	private BasicFunction(EnumFunction type) {
		super(type.notation, type.args);
		this.type = type;
	}
	
	@Override
	public double compute(double[] params) throws CalculationException {
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
		case LN:
			return Math.log(a);
		case SIGN:
			return Math.signum(a);
		case ABS:
			return Math.abs(a);
		case MIN:
			return Math.min(a, params[1]);
		case MAX:
			return Math.max(a, params[1]);
		case LOG:
			return Math.log(params[1]) / Math.log(a);
		}
		return a;
	}
}

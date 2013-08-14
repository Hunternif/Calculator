package hunternif.rpn;

import hunternif.rpn.token.TokenFunction;

public class TestFunction extends TokenFunction {

	public TestFunction() {
		super("maxof3", 3);
	}

	@Override
	public double compute(double[] params) throws CalculationException {
		return Math.max(Math.max(params[0], params[1]), params[2]);
	}

}

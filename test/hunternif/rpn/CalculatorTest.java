package hunternif.rpn;

import junit.framework.Assert;

import org.junit.Test;

public class CalculatorTest {

	@Test
	public void noBracketArithmetic() throws CalculationException {
		Assert.assertEquals(0.1d, Calculator.calculate("-0.1 + 0.2"));
		Assert.assertEquals(4d, Calculator.calculate("2 +\n2"));
		Assert.assertEquals(0d, Calculator.calculate("-1 + 1"));
		Assert.assertEquals(10d, Calculator.calculate("5 * 4 / 2"));
		Assert.assertEquals(0.5d * 5d / 0.5d * 2d, Calculator.calculate("0.5 * 5 / 0.5 * 2"));
		Assert.assertEquals(2d, Calculator.calculate("1 / 2 + 3 * 0.5"));
		Assert.assertEquals(16d, Calculator.calculate("2^4"));
		Assert.assertEquals(4d, Calculator.calculate("2^3/2*1"));
	}
	
	@Test
	public void bracketArithmetic() throws CalculationException {
		Assert.assertEquals(4d, Calculator.calculate("(((2) + 2))"));
		Assert.assertEquals(2d+2d*(2d-3d*(2d-1d)), Calculator.calculate("2+2*(2-3*(2-1))"));
		Assert.assertEquals((1d+2d)*5d/2d - 0.5d*(1d+1d/(1d-(5d-5d))), Calculator.calculate("(1+2)*5/2 - 0.5*(1+1/(1-(5-5)))"));
		Assert.assertEquals(9d, Calculator.calculate("(1+2)*3"));
		Assert.assertEquals(9d, Calculator.calculate("3*(1+2)"));
		Assert.assertEquals(7d, Calculator.calculate("1+2*3"));
	}
	
	@Test
	public void emptyBracket() {
		try {
			Assert.assertEquals(0d, Calculator.calculate("()"));
		} catch (CalculationException e) {
			// This is expected and correct.
			return;
		}
		Assert.fail("Accepted empty brackets");
	}
	
	@Test
	public void unknownFunction() {
		try {
			Calculator.calculate("qwerty(2)");
		} catch (CalculationException e) {
			// This is expected and correct.
			return;
		}
		Assert.fail("Didn't notice unknown function qwerty");
	}
	
	@Test
	public void divideByZero() throws CalculationException {
		Assert.assertEquals(Double.POSITIVE_INFINITY, Calculator.calculate("1/0"));
		Assert.assertEquals(Double.POSITIVE_INFINITY, Calculator.calculate("1/0 + 1"));
	}
	
	@Test
	public void constants() throws CalculationException {
		Assert.assertEquals(Math.PI, Calculator.calculate("pi"));
		Assert.assertEquals(Math.E, Calculator.calculate("e"));
		Assert.assertEquals(Math.PI*Math.E, Calculator.calculate("pi*e"));
		Assert.assertEquals(Math.pow(2d*Math.PI, 3d) - Math.E/2d, Calculator.calculate("(2*pi)^3-e/2"));
	}
	
	@Test
	public void powerOfNegative() throws CalculationException {
		Assert.assertTrue(Double.isNaN(Calculator.calculate("(-1.5)^1.3")));
	}
	
	@Test
	public void functions() throws CalculationException {
		Assert.assertEquals(Math.PI, Calculator.calculate("arccos(-0.5*2)"));
		Assert.assertEquals(2d, Calculator.calculate("arccos(cos(2)+arccos(1))"));
		Assert.assertEquals(Math.exp(2), Calculator.calculate("exp(2)"));
		Assert.assertEquals(2d, Calculator.calculate("exp(ln(2))"));
		Assert.assertEquals(1d, Calculator.calculate("e^ln(1)"));
	}
	
	@Test
	public void functions2() throws CalculationException {
		Assert.assertEquals(2d, Calculator.calculate("max(2, exp(-5))"));
		Assert.assertEquals(2d, Calculator.calculate("max((1+(0.5+0.5)), 0)"));
		Assert.assertEquals(Math.PI-1d, Calculator.calculate("max(e, pi) - min(2, 1)"));
		Assert.assertEquals(0d, Calculator.calculate("2 - exp(log(e, 2))"));
		Assert.assertEquals(1d, Calculator.calculate("e^log(e, 1)"));
	}
	
	@Test
	public void customFunction() throws CalculationException {
		Calculator.registerFunction(new TestFunction());
		Assert.assertEquals(3d, Calculator.calculate("maxof3(1,2,3)"));
		Assert.assertEquals(3d, Calculator.calculate("maxof3(max(0, 1), exp(0-log(2, 3)), 3)"));
	}
}

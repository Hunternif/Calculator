package hunternif.rpn;

import junit.framework.Assert;

import org.junit.Test;

public class RPNTest {

	@Test
	public void noBracketArithmetic() throws RPNCalculationException {
		Assert.assertEquals(0.1d, RPN.calculate("-0.1 + 0.2"));
		Assert.assertEquals(4d, RPN.calculate("2 +\n2"));
		Assert.assertEquals(0d, RPN.calculate("-1 + 1"));
		Assert.assertEquals(10d, RPN.calculate("5 * 4 / 2"));
		Assert.assertEquals(0.5d * 5d / 0.5d * 2d, RPN.calculate("0.5 * 5 / 0.5 * 2"));
		Assert.assertEquals(2d, RPN.calculate("1 / 2 + 3 * 0.5"));
		Assert.assertEquals(16d, RPN.calculate("2^4"));
		Assert.assertEquals(4d, RPN.calculate("2^3/2*1"));
	}
	
	@Test
	public void bracketArithmetic() throws RPNCalculationException {
		Assert.assertEquals(4d, RPN.calculate("(((2) + 2))"));
		Assert.assertEquals(2d+2d*(2d-3d*(2d-1d)), RPN.calculate("2+2*(2-3*(2-1))"));
		Assert.assertEquals((1d+2d)*5d/2d - 0.5d*(1d+1d/(1d-(5d-5d))), RPN.calculate("(1+2)*5/2 - 0.5*(1+1/(1-(5-5)))"));
		Assert.assertEquals(9d, RPN.calculate("(1+2)*3"));
		Assert.assertEquals(9d, RPN.calculate("3*(1+2)"));
		Assert.assertEquals(7d, RPN.calculate("1+2*3"));
	}
	
	@Test
	public void emptyBracket() {
		try {
			Assert.assertEquals(0d, RPN.calculate("()"));
		} catch (RPNCalculationException e) {
			// This is expected and correct.
			return;
		}
		Assert.fail("Accepted empty brackets");
	}
	
	@Test
	public void unknownFunction() {
		try {
			RPN.calculate("cos(2)");
		} catch (RPNCalculationException e) {
			// This is expected and correct.
			return;
		}
		Assert.fail("Didn't notice unknown function cos");
	}
	
	@Test
	public void divideByZero() throws RPNCalculationException {
		Assert.assertEquals(Double.POSITIVE_INFINITY, RPN.calculate("1/0"));
		Assert.assertEquals(Double.POSITIVE_INFINITY, RPN.calculate("1/0 + 1"));
	}
	
	@Test
	public void constants() throws RPNCalculationException {
		Assert.assertEquals(Math.PI, RPN.calculate("pi"));
		Assert.assertEquals(Math.E, RPN.calculate("e"));
		Assert.assertEquals(Math.PI*Math.E, RPN.calculate("pi*e"));
		Assert.assertEquals(Math.pow(2d*Math.PI, 3d) - Math.E/2d, RPN.calculate("(2*pi)^3-e/2"));
	}

}

package hunternif.rpn.token;

public class TokenValue extends Token {
	public double value;
	
	public TokenValue(double value) {
		super(String.valueOf(value));
		this.value = value;
	}
}

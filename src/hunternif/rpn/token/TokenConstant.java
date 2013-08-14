package hunternif.rpn.token;

public class TokenConstant extends TokenValue {
	public TokenConstant(String notation, double value) {
		super(value);
		this.notation = notation;
	}
}

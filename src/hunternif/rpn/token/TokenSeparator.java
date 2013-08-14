package hunternif.rpn.token;

public class TokenSeparator extends Token {
	public static final TokenSeparator COMMA = new TokenSeparator(",");
	
	public TokenSeparator(String notation) {
		super(notation);
	}
}

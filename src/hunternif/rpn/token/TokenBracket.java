package hunternif.rpn.token;

public class TokenBracket extends Token {
	public static final TokenBracket LEFT = new TokenBracket("(");
	public static final TokenBracket RIGHT = new TokenBracket(")");
	public static final TokenBracket COMPLETE = new TokenBracket("()");
	
	protected TokenBracket(String notation) {
		super(notation);
	}
}

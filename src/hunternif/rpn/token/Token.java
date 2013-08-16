package hunternif.rpn.token;

public abstract class Token {
	public static final Token BRACKET_LEFT = new TokenSpecial("(");
	public static final Token BRACKET_RIGHT = new TokenSpecial(")");
	public static final Token BRACKET_COMPLETE = new TokenSpecial("()");
	public static final Token COMMA = new TokenSpecial(",");
	
	public String notation;
	
	public Token(String notation) {
		this.notation = notation;
	}
	
	public static class TokenSpecial extends Token {
		public TokenSpecial(String notation) {
			super(notation);
		}
	}
	
	@Override
	public String toString() {
		return notation;
	}
}

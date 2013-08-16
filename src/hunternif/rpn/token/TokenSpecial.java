package hunternif.rpn.token;

public final class TokenSpecial extends Token {
	public static final Token BRACKET_RIGHT = new TokenSpecial(")", -2);
	public static final Token BRACKET_LEFT = new TokenSpecial("(", 1001);
	public static final Token BRACKET_COMPLETE = new TokenSpecial("()", 1002);
	public static final Token COMMA = new TokenSpecial(",", -1);
	public static final Token BRACKET_FUNCTION_LEFT = new TokenSpecial("(", 10010, "f(");
	
	private String name;
	private TokenSpecial(String notation, int precedence, String name) {
		this(notation, precedence);
		this.name = name;
	}
	private TokenSpecial(String notation, int precedence) {
		super(notation, precedence);
	}
	
	@Override
	public String toString() {
		return name == null ? super.toString() : name;
	}
}

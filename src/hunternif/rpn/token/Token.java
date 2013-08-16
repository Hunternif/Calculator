package hunternif.rpn.token;

public abstract class Token {
	public String notation;
	public int precedence;
	
	public Token(String notation, int precedence) {
		this.notation = notation;
		this.precedence = precedence;
	}
	
	@Override
	public String toString() {
		return notation;
	}
}

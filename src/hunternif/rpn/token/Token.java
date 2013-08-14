package hunternif.rpn.token;

public abstract class Token {
	public String notation;
	
	public Token(String notation) {
		this.notation = notation;
	}
	
	@Override
	public String toString() {
		return notation;
	}
}

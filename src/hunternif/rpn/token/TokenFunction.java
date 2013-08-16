package hunternif.rpn.token;

public abstract class TokenFunction extends TokenComputable {
	public TokenFunction(String notation, int args) {
		super(notation, args, 10000);
	}
	
	@Override
	public boolean isInfix() {
		return false;
	}
	
	@Override
	public String toString() {
		return notation;
	}
}

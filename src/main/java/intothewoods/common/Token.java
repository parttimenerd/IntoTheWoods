package intothewoods.common;
/**
 * A simple token for lexer and parser.
 */
public class Token {

	/**
	 * The type of this token.
	 */
	protected TokenType type;

	/**
	 * The text representing this token or an empty string if it's an imaginary token.
	 */
	protected String text;

	/**
	 * Initialize a new token with the given type and the given text.
	 * @param type given type
	 * @param text given text representing this token or an empty string if it's an imaginary token.
	 */
	public Token(TokenType type, String text) {
		this.type = type;
		this.text = text;
	}

	/**
	 * Initialize a new imaginary token with the given type and en empty string as it's text.
	 * @param type given type
	 */
	public Token(TokenType type) {
		this(type, "");
	}


	/**
	 * Initialize a new NIL token.
	 */
	public Token(){
		this.type = TokenType.NIL;
	}

	@Override
	public String toString() {
		return "Token{" + type + '}';
	}

	/**
	 * Return the current type of this token.
	 * @return current type of this token
	 */
	public TokenType getType(){
		return type;
	}

	/**
	 * Checks whether or not this token has the given type.
	 * @param type given type
	 * @return does this token have the given type?
	 */
	public boolean hasType(TokenType type){
		return this.type == type;
	}

	/**
	 * Return the text representing this token or an empty string if it's an imaginary token
	 * @return text representing this token
	 */
	public String getText(){
		return text;
	}
}
package intothewoods.lexer;

/**
 * A simple lexer token.
 */
public class Token {

	/**
	 * The type of this token.
	 */
	public final TokenType type;
	/**
	 * The text representing this token.
	 */
	public final String text;

	/**
	 * The line this token appears in.
	 */
	public final int line;
	/**
	 * The column this token appears in.
	 */
	public final int column;

	@Override
	public String toString() {
		return "Token{" +
				"type=" + type +
				", text='" + text + '\'' +
				", location=" + line +
				'[' + (column - text.length()) + ',' +
				column + ']' +
				'}';
	}

	public Token(TokenType type, String text, int line, int column) {
		this.type = type;
		this.text = text;
		this.line = line;
		this.column = column;
	}


}
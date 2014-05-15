package intothewoods.lexer;

import intothewoods.common.Token;
import intothewoods.common.TokenType;

/**
 * A simple lexer token.
 */
public class LexerToken extends Token {

	/**
	 * The line this token appears in.
	 */
	private final int line;

	/**
	 * The column this token appears in.
	 */
	private final int column;

	public LexerToken(TokenType type, String text, int line, int column) {
		super(type, text);
		this.line = line;
		this.column = column;
	}

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

	/**
	 * The text representing this token.
	 */
	public String getText() {
		return text;
	}

	/**
	 * The line this token appears in.
	 */
	public int getLine() {
		return line;
	}

	/**
	 * The column this token appears in.
	 */
	public int getColumn() {
		return column;
	}
}
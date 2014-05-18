package intothewoods.parser;

import intothewoods.lexer.LexerToken;

/**
 * Exception raised when the parser spots a syntax error.
 */
public class ParserException extends Exception {

	private final LexerToken token;

	public ParserException(String message, LexerToken token) {
		super("Syntax error at token '" + token.getText() + "' in line " + token.getLine() +
				'[' + token.getColumn() + "]: " + message);
		this.token = token;
	}

	public LexerToken getToken() {
		return token;
	}

}
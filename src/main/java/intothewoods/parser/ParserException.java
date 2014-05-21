package intothewoods.parser;

import intothewoods.lexer.LexerException;
import intothewoods.lexer.LexerToken;

/**
 * Exception raised when the parser spots a syntax error.
 */
public class ParserException extends Exception {

	private final LexerToken token;

	public ParserException(String message, LexerToken token, String currentCodeLine) {
		super(LexerException.composeMessage(message, token.getLine(), token.getColumn(), currentCodeLine));
		this.token = token;
	}

	public LexerToken getToken() {
		return token;
	}

}
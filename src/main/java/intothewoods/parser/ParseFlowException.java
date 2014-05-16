package intothewoods.parser;

import intothewoods.lexer.LexerException;
import intothewoods.lexer.LexerToken;

/**
 * Exception used to direct the parse flow.
 * It's only created once, as exceptions are expensive to create but inexpensive to throw.
 * This class mustn't be used via multiple threads.
 * (See "Language Implementation Patterns" by Terence Parr or
 * https://blogs.atlassian.com/2011/05/if_you_use_exceptions_for_path_control_dont_fill_in_the_stac/)
 */
public class ParseFlowException extends Exception {

	private String message;
	private LexerToken token;
	private static ParseFlowException exception;

	private ParseFlowException(){}

	/**
	 * Returns the exception singleton, with message and token set to the given values.
	 * @param message new message
	 * @param token new token
	 * @return exception singleton
	 */
	public static ParseFlowException getException(String message, LexerToken token){
		if (exception == null){
			exception = new ParseFlowException();
		}
		exception.message = "Error at …" + token.getText() + "… at " + token.getLine() + '['
				+ token.getColumn() + "]: " + message;
		exception.token = token;
		return exception;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public LexerToken getToken(){
		return token;
	}

	@Override
	public Throwable fillInStackTrace()
	{
		return this;
	}
}

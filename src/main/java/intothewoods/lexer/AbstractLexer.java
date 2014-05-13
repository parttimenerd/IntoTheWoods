package intothewoods.lexer;

import java.io.IOException;
import java.io.InputStream;

/**
 * /**
 * Lexer turning an input stream into tokens, ignoring every sort of white space.
 */
public abstract class AbstractLexer {

	protected static final String FUNCTION_KEYWORD = "_function";
	protected static final String RETURN_KEYWORD = "_return";
	protected static final String IF_KEYWORD = "_if";
	protected static final String ELSE_KEYWORD = "_else";
	protected static final String WHILE_KEYWORD = "_while";
	protected static final String END_KEYWORD = "_end";


	protected char currentChar;
	protected int currentLine;
	protected int currentColumn;
	protected boolean hasEnded;
	protected final InputStream input;
	protected Token token;

	/**
	 * Initializes a lexer reading from the given stream.
	 * Reads the first token from the stream.
	 *
	 * @param input given input stream
	 * @throws LexerException syntax error
	 * @throws java.io.IOException io error from stream
	 */
	public AbstractLexer(InputStream input) throws LexerException, IOException {
		this.input = input;
		currentChar = 0;
		currentLine = 1;
		currentColumn = -1;
		hasEnded = false;
		nextToken();
	}

	/**
	 * Capture the next token from the input stream, stores and returns it.
	 * @return next token or token of type EOF if there is no next token
	 * @throws LexerException syntax error
	 * @throws java.io.IOException io error from stream
	 */
	public abstract Token nextToken() throws LexerException, IOException;

	/**
	 * Return the current token.
	 * @return current token
	 */
	public Token getToken(){
		return token;
	}

}
